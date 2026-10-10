# Usage: 'shell env.sh'

# Try to accommodate Mac and Linux
platform=$(uname | tr '[:upper:]' '[:lower:]' | sed 's/darwin/macosx/')
machine=$(uname -m | tr '[:upper:]' '[:lower:]' | sed 's/arm64/aarch64/' | sed 's/x86_64/amd64/')

# These are useful aliases while developing
alias asu="java -jar ${PWD}/tools/asu/build/libs/applesingle-tools-asu-*.jar"
alias asun="${PWD}/tools/asu/build/native/nativeCompile/asu-${platform}-${machine}-*"
