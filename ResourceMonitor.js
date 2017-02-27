var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var WebSocketServer = require('websocket').server;
var os = require('os');
var drivelist = require('drivelist');
var diskspace = require('diskspace');
var oldcpus = os.cpus();
var connections = [];
var currentdisks = [];
const port = (process.env.PORT || 5000);
var _interval = 1;
var TOTAL_PERCENT = 100;

function isEmpty(obj) {
    return Object.getOwnPropertyNames(obj).length == 0;
}

(function (ResourceMonitor) {

    ResourceMonitor.init = function (interval) {

        app.get('/cpucount', function (req, res) {
            var cpuCount = {count: os.cpus().length};
            res.send(cpuCount);
        });
        app.get('/availableDisks', function (req, res) {

            getDisks(function (error, disks) {

                var _resp = {error, disks};

                if (isEmpty(_resp.disks)) {

                    _resp.error = "empty disks";
                    return res.send(_resp);
                }


                currentdisks = _resp.disks;


                return res.send(_resp);
            });

        });

        app.get('/', function (req, res) {
            res.sendFile(__dirname + '/index.html');
        });

        http.listen(port, function () {


            StartEmitting(interval);
            //initclassicWs();
            io.on('connection', function (socket) {

                socket.on('disconnect', function () {
                    //console.log('socket %s has disconnected' .socket.id);

                });
            });

        });

    };

    function initclassicWs() {

        var server = require('http').createServer(function (request, response) {
            console.log((new Date()) + ' Received request for ' + request.url);
            response.writeHead(404);
            response.end();
        });
        server.listen(5500, function () {
            console.log((new Date()) + ' Classic websocket on port ' + 5000);
        });

        wsServer = new WebSocketServer({
            httpServer: server,
            // You should not use autoAcceptConnections for production
            // applications, as it defeats all standard cross-origin protection
            // facilities built into the protocol and the browser.  You should
            // *always* verify the connection's origin and decide whether or not
            // to accept it.
            autoAcceptConnections: false
        });

        wsServer.on('request', function (request) {
            console.log(request);
            var connection = request.accept('echo-protocol', request.origin);
            connections.push(connection);
            console.log((new Date()) + ' Connection accepted.');
            connection.on('message', function (message) {
                if (message.type === 'utf8') {
                    console.log('Received Message: ' + message.utf8Data);
                    connection.sendUTF(message.utf8Data);
                }
                else if (message.type === 'binary') {
                    console.log('Received Binary Message of ' + message.binaryData.length + ' bytes');
                    connection.sendBytes(message.binaryData);
                }
            });

            connection.on('close', function (reasonCode, description) {
                console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
                //implement removal of connection here
            });


        })
    }

    function StartEmitting(interval) {
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
        var cpusArray = [];
        CPUsTimeDiffs.forEach(function (cpuTimediff, cpuIndex) {
            var cpuData = {
                cpu: (cpuIndex),
                usedPercent: cpuTimediff
            }
            cpusArray.push(cpuData);
            //console.log("emitting CPU%d info on socket CPU%d:", cpuData.cpu,cpuData.cpu);
            io.emit(cpuData.cpu, cpuData)
        });

        connections.forEach(function (connection) {
            // console.log(connection);

            if (connection) {
                connection.sendUTF(JSON.stringify({CPUDATA: cpusArray}));
            }
        });
        oldcpus = os.cpus();
    }
    function emitRaminfoThroughSocket(){
        io.emit('RAMusagePercent', calculateUsedMemoryPercent())
        connections.forEach(function (connection) {
            if(connection){
                connection.sendUTF("RAMusagePercent:" + calculateUsedMemoryPercent());
            }
        });
    }
    function calculateUsedMemoryPercent(){

        var freeMemoryPercent = (( os.freemem()  / os.totalmem())*100)
        var usedMemorypercent =  TOTAL_PERCENT - freeMemoryPercent
        // console.log("memory used percent %d %:",usedMemorypercent)
        return usedMemorypercent;
    }

    function emitDiskSpace(){
        if (currentdisks.length) {
            currentdisks.forEach(function (disk) {
                var mountPath = disk.mountpoint.split(',')[0];

                diskspace.check(mountPath, function (err, total, free, status) {
                    if (err) {
                        console.log(err)
                    }
                    var freeDiskPercent = (free / total) * 100
                    var usedDiskSpacePercent = TOTAL_PERCENT - freeDiskPercent;
                    io.emit(mountPath, usedDiskSpacePercent);

                    //console.log('used space for drive %s %d %',mountPath,usedDiskSpacePercent);
                });

            });
        }
    }
    function getDisks(ongetDisks){

        drivelist.list(ongetDisks);
    }

})(module.exports);