#
echoerr() { 
	echo "$@" 1>&2; 
	}
logLine(){
	local wNow=$(date +"%F %T");
	echo "${wNow} | $1";
}
logErr(){
	local wNow=$(date +"%F %T");
	echoerr "${wNow} | >>>>> ERROR: $1"
}
logLineSepBold(){
	logLine "##########################################################################################"
}
logLineSepMedium(){
	logLine "==========================================================================================="
}
logLineSepLight(){
	logLine "-------------------------------------------------------------------------------------------"
}
logTitle() {
	logLine "";
	logLineSepBold;
	logLine "$1";
	logLineSepBold;
	logLine "";
}
logStep() {
	logLine "";
	logLineSepLight;
	logLine "$1";
	logLineSepLight;
	logLine "";
}
logTree(){
	logLine "";
	logLineSepMedium "==========";
	logLine "Tree from  pwd:[`pwd`] : ";
	logLine "`tree . -L 3`";
	logLineSepMedium "==========";
	logLine "";
}
#