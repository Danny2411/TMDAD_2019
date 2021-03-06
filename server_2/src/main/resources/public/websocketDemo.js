//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/chat" + location.href.substr(location.href.indexOf('?')));
webSocket.onmessage = function (msg) { updateChat(msg); };
webSocket.onclose = function () { alert("WebSocket connection closed") };

//Send message if "Send" is clicked
id("send").addEventListener("click", function () {
    sendMessage(id("message").value);
});

//Send message if enter is pressed in the input field
id("message").addEventListener("keypress", function (e) {
    if (e.keyCode === 13) { sendMessage(e.target.value); }
});

//Send file if "Upload" is clicked
id("upload").addEventListener("click", function () {
    sendFile();
});

//Send a message if it's not empty, then clear the input field
function sendMessage(message) {
    if (message !== "") {
        webSocket.send(message);
        id("message").value = "";
    }
}

//Update the chat-panel, and the list of connected users
function updateChat(msg) {
    var data = JSON.parse(msg.data);
    if(data.userMessage.includes("CLEAR CHAT")){
    	clearBox("chat");
    } else {
	    if(data.userMessage.includes("Te has unido")){
	    	clearBox("chat");
	    }
	    if(data.userMessage.includes("Abandonando")){
	    	clearBox("chat");
	    }
	    if(data.userMessage.includes("Sala creada")){
	    	clearBox("chat");
	    }
	    insert("chat", data.userMessage);
	}
	
	if(data.isfile.includes("y")){

    	var arr = data.file;
		var byteArray = new Uint8Array(arr);
		var a = window.document.createElement('a');
		
		a.href = window.URL.createObjectURL(new Blob([byteArray], { type: 'application/octet-stream' }));
		a.download = data.route;
		
		// Append anchor to body.
		document.body.appendChild(a);
		a.click();
		
		// Remove anchor from body
		document.body.removeChild(a);
    }
    
    id("userlist").innerHTML = "";
    data.userlist.forEach(function (user) {
        insert("userlist", "<li>" + user + "</li>");
    });
    
    id("currentchannel").innerHTML = "";
    insert("currentchannel", "<li>" + data.currentchannel + "</li>");
    
    id("yourname").innerHTML = "";
    insert("yourname", "<li>" + data.yourname + "</li>");
    
    id("notificationlist").innerHTML = "";
    insert("notificationlist", "<li>" + data.notificationlist + "</li>");
 
}

// Send file function
function sendFile() {
	var file = document.getElementById('filename').files[0];
	var reader = new FileReader();
    var rawData = new ArrayBuffer();            
    reader.loadend = function() {
    
    }
    reader.onload = function(e) {
        rawData = e.target.result;
        webSocket.send("!FILE:" + file.name);
        webSocket.send(rawData);
    }
    reader.readAsArrayBuffer(file);
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}

//Clear chat
function clearBox(elementID)
{
    document.getElementById(elementID).innerHTML = "";
}
