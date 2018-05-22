//--------------------
// GET USER MEDIA CODE
//--------------------
navigator.getUserMedia = ( navigator.getUserMedia ||
    navigator.webkitGetUserMedia ||
    navigator.mozGetUserMedia ||
    navigator.msGetUserMedia);

var video;
var webcamStream;

function startWebcam() {
    if (navigator.getUserMedia) {
        navigator.getUserMedia (

            // constraints
            {
                video: true,
                audio: false
            },

            // successCallback
            function(localMediaStream) {
                video = document.querySelector('video');
                video.src = window.URL.createObjectURL(localMediaStream);
                webcamStream = localMediaStream;
            },

            // errorCallback
            function(err) {
                console.log("The following error occured: " + err);
            }
        );
    } else {
        console.log("getUserMedia not supported");
    }
}

function stopWebcam() {
    webcamStream.stop();
}
//---------------------
// TAKE A SNAPSHOT CODE
//---------------------
var canvas, ctx;

function init() {
    // Get the canvas and obtain a context for
    // drawing in it
    canvas = document.getElementById("myCanvas");
    ctx = canvas.getContext('2d');
    startWebcam();
}

function snapshot() {
    // Draws current image from the video element into the canvas
    ctx.drawImage(video, 0,0, canvas.width, canvas.height);
}

function uploadSignIn(filename) {
    var dataURL = canvas.toDataURL();
    var blobBin = atob(dataURL.split(',')[1]);
    var token =  $('input[name="csrfToken"]').attr('value');
    var array = [];
    for(var i = 0; i < blobBin.length; i++) {
        array.push(blobBin.charCodeAt(i));
    }
    var file=new Blob([new Uint8Array(array)], {type: 'image/png'});
    var formdata = new FormData();
    formdata.append("email", $("#email").val());
    formdata.append("password", $("#password").val());
    formdata.append("uploadedImage", file, filename);
    $.ajax({
        type: "POST",
        url: '/signIn',
        headers: { 'IsAjax': 'true' },
        processData: false,
        contentType: false,
        data: formdata,
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        },
        success: function (result) {
            console.log("RES: " + result);
            document.open();
            document.write(result);
            document.close();
        },
        error: function (error) {
            onErrorUpload(error);
        }
    });
}

function uploadSignUp(filename) {
    var dataURL = canvas.toDataURL();
    var blobBin = atob(dataURL.split(',')[1]);
    var token =  $('input[name="csrfToken"]').attr('value');
    var array = [];
    for(var i = 0; i < blobBin.length; i++) {
        array.push(blobBin.charCodeAt(i));
    }
    var file=new Blob([new Uint8Array(array)], {type: 'image/png'});
    var formdata = new FormData();
    formdata.append("firstName", $("#firstName").val());
    formdata.append("lastName", $("#lastName").val());
    formdata.append("email", $("#email").val());
    formdata.append("password", $("#password").val());
    formdata.append("uploadedImage", file, filename);
    $.ajax({
        type: "POST",
        url: '/signUp',
        headers: { 'IsAjax': 'true' },
        processData: false,
        contentType: false,
        data: formdata,
        beforeSend: function(xhr) {
            xhr.setRequestHeader('Csrf-Token', token);
        },
        success: function (result) {
            onSuccessUpload(result);
        },
        error: function (error) {
            onErrorUpload(error);
        }
    });
}
var onSuccessUpload = function (result) {
    $(".alert-info").css("display", "block");
    $("#infoText").text(result);
};
var onErrorUpload = function (error) {
    console.log("error: " + JSON.stringify(error));
};

function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

$('#mainForm').one('submit', function(ev) {
    ev.preventDefault();
    var uuid = uuidv4();
    snapshot();
    uploadSignUp(uuid);
    stopWebcam();
    $(this).submit();
});

$('#loginForm').one('submit', function(ev) {
    ev.preventDefault();
    var uuid = uuidv4();
    snapshot();
    uploadSignIn(uuid);
    stopWebcam();
    $(this).submit();
});

$( document ).ready(function() {
    init();
    console.log( "ready!" );
});