function fatal {
  comment_on_pull "Tests failed: $1"
  echo "$1"
  exit 1
}

function comment_on_pull
{
    if [ "$COMMENT_ON_PULL" = "" ]; then return; fi

    PULL_NUMBER=$(echo $GIT_BRANCH | awk -F 'pull' '{ print $2 }' | awk -F '/' '{ print $2 }')
    if [ "$PULL_NUMBER" != "" ]
    then
        JSON="{ \"body\": \"$1\" }"
        curl -d "$JSON" -ujbosstm-bot:$BOT_PASSWORD https://api.github.com/repos/$GIT_ACCOUNT/$GIT_REPO/issues/$PULL_NUMBER/comments
    else
        echo "Not a pull request, so not commenting"
    fi
}

#BUILD NARAYANA WITH FINDBUGS
function build_narayana {
  echo "Building Narayana"
  cd $WORKSPACE
  [ $NARAYANA_TESTS = 1 ] && NARAYANA_ARGS= || NARAYANA_ARGS="-DskipTests"

  ./build.sh -Dfindbugs.skip=false -Dfindbugs.failOnError=false "$@" $NARAYANA_ARGS $IPV6_OPTS clean install
  [ $? = 0 ] || fatal "narayana build failed"
  cp_narayana_to_as

  return 0
}

function cp_narayana_to_as {
  echo "Copying Narayana to AS"
  cd $WORKSPACE
  JBOSS_VERSION=`ls -1 ${WORKSPACE}/jboss-as/build/target | grep jboss-as`
  [ $? = 0 ] || return 1
  export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/${JBOSS_VERSION}
  [ -d $JBOSS_HOME ] || return 1

  echo "WARNING - check that narayana version ${NARAYANA_VERSION} is the one you want"
  JAR1=jbossjts-integration-${NARAYANA_VERSION}.jar
  JAR2=jbossjts-${NARAYANA_VERSION}.jar
# TODO make sure ${JBOSS_HOME} doesn't already contain a different version of narayana
  echo "cp ./ArjunaJTS/integration/target/$JAR1 ${JBOSS_HOME}/modules/org/jboss/jts/integration/main/"
  echo "cp ./ArjunaJTS/narayana-jts/target/$JAR2 ${JBOSS_HOME}/modules/org/jboss/jts/main/"
  cp ./ArjunaJTS/integration/target/$JAR1 ${JBOSS_HOME}/modules/org/jboss/jts/integration/main/
  [ $? = 0 ] || return 1
  cp ./ArjunaJTS/narayana-jts/target/$JAR2 ${JBOSS_HOME}/modules/org/jboss/jts/main/
}

function build_as {
  echo "Building AS"
  GIT_URL="https://github.com/jbosstm/jboss-as.git"
  UPSTREAM_GIT_URL="https://github.com/jbossas/jboss-as.git"

  cd ${WORKSPACE}
  if [ -d jboss-as ]; then
    echo "Updating existing checkout of AS7"
    cd jboss-as
    #Abort any partially complete rebase
    git rebase --abort
    git checkout 5_BRANCH
    [ $? = 0 ] || fatal "git checkout 5_BRANCH failed"
    git fetch
    [ $? = 0 ] || fatal "git fetch $GIT_URL failed"
    git reset --hard origin/5_BRANCH
    [ $? = 0 ] || fatal "git reset 5_BRANCH failed"
    git clean -f -d -x
    [ $? = 0 ] || fatal "git clean failed"
    git rebase --abort
    rm -rf .git/rebase-apply
  else
    echo "First time checkout of AS7"
    git clone $GIT_URL
    [ $? = 0 ] || fatal "git clone $GIT_URL failed"

    cd jboss-as
    git checkout -t origin/5_BRANCH
    [ $? = 0 ] || fatal "git checkout 5_BRANCH failed"

    git remote add upstream $UPSTREAM_GIT_URL
  fi

  git pull --rebase --ff-only upstream master
  [ $? = 0 ] || fatal "git rebase failed"

  export MAVEN_OPTS="$MAVEN_OPTS -XX:MaxPermSize=512m"
  export JAVA_OPTS="$JAVA_OPTS -Xms1303m -Xmx1303m -XX:MaxPermSize=512m"
  ./build.sh clean install -DskipTests -Dts.smoke=false $IPV6_OPTS
  [ $? = 0 ] || fatal "AS build failed"
  init_jboss_home
}

function init_jboss_home {
  cd $WORKSPACE
  JBOSS_VERSION=`ls -1 ${WORKSPACE}/jboss-as/build/target | grep jboss-as`
  [ $? = 0 ] || fatal "missing AS - cannot set JBOSS_VERSION"
  export JBOSS_HOME=${WORKSPACE}/jboss-as/build/target/${JBOSS_VERSION}
  [ -d $JBOSS_HOME ] || fatal "missing AS - $JBOSS_HOME is not a directory"
  echo "JBOSS_HOME=$JBOSS_HOME"
  cp ${JBOSS_HOME}/docs/examples/configs/standalone-xts.xml ${JBOSS_HOME}/standalone/configuration
}

function xts_as_tests {
  echo "#-1. XTS AS Integration Test"
  cd ${WORKSPACE}/jboss-as
  ./build.sh -f ./testsuite/integration/xts/pom.xml -Pxts.integration.tests.profile "$@" test
  [ $? = 0 ] || fatal "XTS AS Integration Test failed"
  cd ${WORKSPACE}
}

function txframework_tests {
  echo "#0. TXFramework Test"
  cp ./rest-tx/webservice/target/restat-web-*.war $JBOSS_HOME/standalone/deployments
  ./build.sh -f ./txframework/pom.xml -P$ARQ_PROF "$@" test
  [ $? = 0 ] || fatal "TxFramework build failed"
}

function xts_tests {
  echo "#1 XTS: WSTX11 INTEROP, UNIT TESTS, xtstest and CRASH RECOVERY TESTS"

  cd $WORKSPACE
  ran_crt=1

  if [ $WSTX_MODULES ]; then
    [[ $WSTX_MODULES = *crash-recovery-tests* ]] || ran_crt=0
    echo "BUILDING SPECIFIC WSTX11 modules"
    ./build.sh -f XTS/localjunit/pom.xml --projects "$WSTX_MODULES" -P$ARQ_PROF "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
  else
    ./build.sh -f XTS/localjunit/pom.xml -P$ARQ_PROF "$@" $IPV6_OPTS -Dorg.jboss.remoting-jmx.timeout=300 clean install "$@"
  fi

  [ $? = 0 ] || fatal "XTS: SOME TESTS failed"
  if [ $ran_crt = 1 ]; then
    if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
      (cd XTS/localjunit/crash-recovery-tests && java -cp target/classes/ com.arjuna.qa.simplifylogs.SimplifyLogs ./target/log/ ./target/log-simplified)
      [ $? = 0 ] || fatal "Simplify CRASH RECOVERY logs failed"
    fi
  fi
}

function tx_bridge_tests {
  echo "XTS: TXBRIDGE TESTS update conf"
  cd $WORKSPACE
  CONF="${JBOSS_HOME}/standalone/configuration/standalone-xts.xml"
  grep recovery-listener "$CONF"
  sed -e s/recovery-listener=\"true\"//g -i $CONF
  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i $CONF

#  sed -e "s#\(recovery-environment\) \(socket-binding\)#\\1 recovery-listener=\"true\" \\2#" -i $CONF
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS: sed failed"

  echo "XTS: TXBRIDGE TESTS"
  ./build.sh -f txbridge/pom.xml -P$ARQ_PROF "$@" $IPV6_OPTS clean install "$@"
  [ $? = 0 ] || fatal "#3.TXBRIDGE TESTS failed"
}

function qa_tests_once {
  echo "QA Test Suite $@"
  cd $WORKSPACE/qa
  unset orb

  # look for an argument of the form orb=<something>
  for i in $@; do
    [ ${i%%=*} = "orb" ] && orb=${i##*=}
  done

  # check to see if we were called with orb=idlj as one of the arguments
  if [ x$orb = x"idlj" ]; then
    IDLJ=1
    testoutputzip="testoutput-idlj.zip"
  else
    IDLJ=0
    testoutputzip="testoutput-jacorb.zip"
  fi

  git checkout TaskImpl.properties
  sed -i TaskImpl.properties -e "s#^COMMAND_LINE_0=.*#COMMAND_LINE_0=${JAVA_HOME}/bin/java#"
  [ $? = 0 ] || fatal "sed TaskImpl.properties failed"

  # delete lines containing jacorb
  [ $IDLJ = 1 ] && sed -i TaskImpl.properties -e  '/^.*separator}jacorb/ d'

  # if the env variable MFACTOR is set then set the bean property CoreEnvironmentBean.timeoutFactor
  [ -z "${MFACTOR+x}" ] || sed -i TaskImpl.properties -e "s/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=[0-9]*/COMMAND_LINE_12=-DCoreEnvironmentBean.timeoutFactor=${MFACTOR}/"

  # if IPV6_OPTS is not set get the jdbc drivers (we do not run the jdbc tests in IPv6 mode)
  [ -z "${IPV6_OPTS+x}" ] && ant -DisIdlj=$IDLJ "$QA_BUILD_ARGS" get.drivers dist ||
    ant -DisIdlj=$IDLJ "$QA_BUILD_ARGS" dist

  [ $? = 0 ] || fatal "qa build failed"

  if [[ $# == 0 || $# > 0 && "$1" != "-DskipTests" ]]; then
    # determine which QA test target to call
    target="ci-tests" # the default is to run everything (ci-tests)

    # if IPV6_OPTS is set then do not do the jdbc tests (ie run target junit-testsuite)
    [ -z "${IPV6_OPTS+x}" ] || target="junit-testsuite"

    # IDLJ = 1 overrides the previous setting 
    [ $IDLJ = 1 ] && target="ci-jts-tests" # if called with orb=idlj then only run the jtsremote tests

    # QA_TARGET overrides the previous settings
    [ x$QA_TARGET = x ] || target=$QA_TARGET # the caller can force the build to run a specific target

    # run the ant target
    ant -f run-tests.xml $target
    ok=$?
    # archive the jtsremote test output (use a name related to the orb that was used for the tests)
    ant -f run-tests.xml testoutput.zip -Dtestoutput.zipname=$testoutputzip
    return $ok
  fi
  return 0
}

function qa_tests {
  ok1=0;
  ok2=0;
  if [ $SUN_ORB = 1 ]; then
    qa_tests_once "$@" "orb=idlj" "$@" # run qa against the Sun orb
    ok2=$?
  fi
  if [ $JAC_ORB = 1 ]; then
    qa_tests_once "$@"    # run qa against the default orb
    ok1=$?
  fi

  [ $ok1 = 0 ] || echo some jacorb QA tests failed
  [ $ok2 = 0 ] || echo some Sun ORB QA tests failed

  [ $ok1 = 0 -a $ok2 = 0 ] || fatal "some qa tests failed"
}

comment_on_pull "Started testing this pull request: $BUILD_URL"

# if the following env variables have not been set initialize them to their defaults
[ $NARAYANA_VERSION ] || NARAYANA_VERSION="5.0.0.M2-SNAPSHOT"
[ $ARQ_PROF ] || ARQ_PROF=arq	# IPv4 arquillian profile

[ $NARAYANA_TESTS ] || NARAYANA_TESTS=1	# run the narayana surefire tests
[ $NARAYANA_BUILD ] || NARAYANA_BUILD=1 # build narayana
[ $AS_BUILD ] || AS_BUILD=1 # git clone and build a fresh copy of the AS
[ $TXF_TESTS ] || TXF_TESTS=0 # TxFramework tests
[ $XTS_TESTS ] || XTS_TESTS=1 # XTS tests
[ $XTS_AS_TESTS ] || XTS_AS_TESTS=1 # XTS tests
[ $QA_TESTS ] || QA_TESTS=1 # QA test suite
[ $SUN_ORB ] || SUN_ORB=1 # Run QA test suite against the Sun orb
[ $JAC_ORB ] || JAC_ORB=1 # Run QA test suite against JacORB
[ $txbridge ] || txbridge=1 # bridge tests
# if QA_BUILD_ARGS is unset then get the db drivers form the file system otherwise get them from the
# default location (see build.xml). Note ${var+x} substitutes null for the parameter if var is undefined
[ -z "${QA_BUILD_ARGS+x}" ] && QA_BUILD_ARGS="-Ddriver.url=file:///home/hudson/dbdrivers"

# Note: set QA_TARGET if you want to override the QA test ant target

# for IPv6 testing use export ARQ_PROF=arqIPv6
# if you don't want to run all the XTS tests set WSTX_MODULES to the ones you want, eg:
# export WSTX_MODULES="WSAS,WSCF,WSTX,WS-C,WS-T,xtstest,crash-recovery-tests"

[ -z "${WORKSPACE}" ] && fatal "UNSET WORKSPACE"

# FOR DEBUGGING SUBSEQUENT ISSUES
free -m

#Make sure no JBoss processes running
for i in `ps -eaf | grep java | grep "standalone*.xml" | grep -v grep | cut -c10-15`; do kill $i; done

# if we are building with IPv6 tell ant about it
export ANT_OPTS="$ANT_OPTS $IPV6_OPTS"

# run the job
[ $NARAYANA_BUILD = 1 ] && build_narayana "$@"
[ $AS_BUILD = 1 ] && build_as "$@" || init_jboss_home
[ $XTS_AS_TESTS = 1 ] && xts_as_tests
[ $TXF_TESTS = 1 ] && txframework_tests "$@"
[ $XTS_TESTS = 1 ] && xts_tests "$@"
[ $txbridge = 1 ] && tx_bridge_tests "$@"
[ $QA_TESTS = 1 ] && qa_tests "$@"

comment_on_pull "All tests passed - Job complete"
exit 0 # any failure would have resulted in fatal being called which exits with a value of 1
