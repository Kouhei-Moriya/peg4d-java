argv = process.argv;

var PEG = require("./" + argv[2]);
var file = argv[3];

var fs = require('fs');
fs.readFile(file, 'utf8', function (err, text) {
	PEG.parse(text);
});
