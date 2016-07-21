var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var os = require('os');
var drivelist = require('drivelist');
var diskspace = require('diskspace');
var oldcpus = os.cpus();
var currentdisks = []
var _interval = 1;
var TOTAL_PERCENT = 100;
(function (ResourceMonitor) {

ResourceMonitor.init=function(interval){

app.get('/cpucount', function(req, res){
    var cpuCount = { count:os.cpus().length }
    res.send(cpuCount);
});
app.get('/availableDisks', function(req, res){

    getDisks(function(error,disks){
        currentdisks = disks;
        res.send(disks);
    }); 

});

app.get('/', function(req, res){
  res.sendFile(__dirname + '/index.html');
});

   http.listen(3000, function(){

    StartEmitting(interval);   
    io.on('connection',function(socket){

    socket.on('disconnect',function(){
        //console.log('socket %s has disconnected' .socket.id);
     
        });
    });      
});

}
function StartEmitting(interval){
    console.log('one single interval run, start emitting signals!');
        if (interval) {
            _interval = interval;
        }
  setInterval(function () {

        emitCPUInfoThroughSocket();
        emitRaminfoThroughSocket();
        emitDiskSpace();

    }, (_interval * 1000));
}



    
    function calculateCPUsTimeDiffs() {
        var cpus = os.cpus();
        var CPUsTimeDiffs = [];

        cpus.forEach(function (cpu, cpuindex) {
           // console.log("CPU %s:", cpuindex);
            var oldcpu = oldcpus[cpuindex];

            Object.keys(cpu.times).forEach(function (CpuTimetype) {
                var cpuTimediff = getCpuTimeDiff(cpu.times[CpuTimetype], oldcpu.times[CpuTimetype]);
                if (CpuTimetype == 'idle') {
                    CPUsTimeDiffs[cpuindex] = cpuTimediff;
                }
            });
        });
        return CPUsTimeDiffs;
    }

    function getCpuTimeDiff(currentCpuTime, oldcpuTime) {
        var cpuTimediff = ((currentCpuTime - oldcpuTime)) / (_interval * 100);
        if (isNotWindowsOS()) {
            cpuTimediff = 100 - cpuTimediff;
        }
        return cpuTimediff;
    }

    function isNotWindowsOS() {
        return os.platform() != 'win32';
    }

    function emitCPUInfoThroughSocket(){
        var CPUsTimeDiffs = calculateCPUsTimeDiffs();
        CPUsTimeDiffs.forEach(function (cpuTimediff, cpuIndex) {
            var cpuData = {
                cpu: (cpuIndex),
                usedPercent: cpuTimediff
            }
            console.log("emitting CPU%d info on socket CPU%d:", cpuData.cpu,cpuData.cpu);
            io.emit(cpuData.cpu, cpuData)
        });
        oldcpus = os.cpus();
    }
    function emitRaminfoThroughSocket(){
      io.emit('RAMusagePercent', calculateUsedMemoryPercent())   
    }
    function calculateUsedMemoryPercent(){
   
        var freeMemoryPercent = (( os.freemem()  / os.totalmem())*100)
        var usedMemorypercent =  TOTAL_PERCENT - freeMemoryPercent
            console.log("memory used percent %d %:",usedMemorypercent)
   return usedMemorypercent;
    }

    function emitDiskSpace(){

        currentdisks.forEach(function(disk){
        var mountPath = disk.mountpoint.split(',')[0];
        console.log(mountPath);
        diskspace.check(mountPath, function (err, total, free, status)
          {
              if(err){
                  console.log(err)
              }
              var freeDiskPercent = (free / total)*100
              var usedDiskSpacePercent = TOTAL_PERCENT - freeDiskPercent;
              io.emit(mountPath,usedDiskSpacePercent);
             console.log('used space for drive %s %d %',mountPath,usedDiskSpacePercent);                          
          });
           
        });
    }
    function getDisks(ongetDisks){

        drivelist.list(ongetDisks);
    }

})(module.exports);