#!/bin/bash

jvmCount=$#;
jvms=($@)

red="\e[31m"
green="\e[32m"
blue="\e[34m"
reset="\e[0m"

function javaVersion {
    local javaExecutable=$1
    local version=$($javaExecutable -version|&head -1|sed 's/.*"\(.*\)".*/\1/')
    case $version in
        1.7*) echo 7 ;;
        1.8*) echo 8 ;;
        9) echo 9 ;;
        10*) echo 10 ;;
        11*) echo 11 ;;
        *) echo unable to parse java version $version >&2; exit 1 ;;
    esac
}

rm -f buildStatuses;

declare -A javaVersions
for jvm in $@; do
    javaVersions[$jvm]=$(javaVersion $jvm/bin/java)
done

for jvm in $@; do
    echo $jvm "->" ${javaVersions[$jvm]}
done

for testRunnerJvm in $@; do
    for testTargetJvm in $@; do
        if [[ ${javaVersions[$testTargetJvm]} -le ${javaVersions[$testRunnerJvm]} ]]; then
            javaVersion=${javaVersions[$testTargetJvm]}
            # maven-surefire-plugin will run tests using the java executable defined via ${jvm}
            command="mvn clean -Djvm=$testRunnerJvm/bin/java -DtestJavac=${testTargetJvm}/bin/javac -Djava.test.version=$javaVersion test";
            echo -e "running ${blue}$command${reset}"
            echo
            eval $command && result="${green}passed${reset}" || result="${red}failed${reset}"
            echo -e $command $result >> buildStatuses
        fi;
    done;
done;
