
set LIB_FOLDER=../lib
set CLASSPATH=%LIB_FOLDER%/transactions-jdbc.jar;%LIB_FOLDER%/atomikos-util.jar;%LIB_FOLDER%/transactions-api.jar;%LIB_FOLDER%/transactions-jta.jar;%LIB_FOLDER%/transactions.jar;%LIB_FOLDER%/mysql-connector-java-5.1.15-bin.jar;%LIB_FOLDER%/xapool-1.6.beta.jar;%LIB_FOLDER%/ow2-connector-1.5-spec.jar;%LIB_FOLDER%/commons-logging-api.jar;%LIB_FOLDER%/carol-interceptors.jar;%LIB_FOLDER%/carol.jar;%LIB_FOLDER%/jotm-core.jar;%LIB_FOLDER%/junit-4.5.jar;%LIB_FOLDER%/ow2-jta-1.1-spec.jar;../build/classes;../build/test/classes;.

java -classpath %CLASSPATH%  scaling.TestScalingJTAMainLaunch

pause
