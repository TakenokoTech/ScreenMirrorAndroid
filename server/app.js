/*==================================================================================*/
// const video = document.getElementById('v');
// video.src = URL.createObjectURL(mediaSource);
const image = document.getElementById('image');
const submitButton = document.getElementById('submit')
const websocketInput = document.getElementById('websocketInput')

/*==================================================================================*/
const mediaSource = new MediaSource();
mediaSource.addEventListener('sourceended', function(e) {
  console.log('sourceended: ' + mediaSource.readyState);
});
mediaSource.addEventListener('sourceclose', function(e) {
  console.log('sourceclose: ' + mediaSource.readyState);
});
mediaSource.addEventListener('error', function(e) {
  console.log('error: ' + mediaSource.readyState);
});
mediaSource.addEventListener('sourceopen', (e) => {
  console.log('sourceopen: ', e);
  sourceopen();
});

function readAsArrayBuffer(blob) {
  return new Promise(resolve => {
    const reader = new FileReader();
    reader.readAsArrayBuffer(blob);
    reader.addEventListener('loadend', e => {
      resolve(reader.result);
    });
  });
}

function sourceopen(e) {
  const codic = 'video/webm; codecs="vorbis,vp8"'
  const sourceBuffer = mediaSource.addSourceBuffer(codic);
  sourceBuffer.addEventListener('updatestart', (e) => {
    console.log('updatestart: ' + mediaSource.readyState);
  });
  sourceBuffer.addEventListener('update', (e) => {
    console.log('update: ' + mediaSource.readyState);
  });
  sourceBuffer.addEventListener('updateend', (e) => {
    console.log('updateend: ' + mediaSource.readyState);
  });
  sourceBuffer.addEventListener('error', (e) => {
    console.log('error: ' + mediaSource.readyState);
  });
  sourceBuffer.addEventListener('abort', (e) => {
    console.log('abort: ' + mediaSource.readyState);
  });
}

let socket = null
function webSocket(ws) {
  socket = new WebSocket(ws);
  socket.addEventListener('open', e => {
    console.log('open');
    setInterval(() => socket.send('polling'), 60);
  });
  socket.addEventListener('message', e => {
    const data = e.data
    image.src = URL.createObjectURL(data)
    //readAsArrayBuffer(d).then(buffer => {
      //console.log(buffer);
      // sourceBuffer.appendBuffer(buffer);
      // image.src = buffer
    //});
  });
    socket.addEventListener('close', e => {
      console.log('close', e);
    });
    socket.addEventListener('error', e => {
      console.log('error', e);
    });
}

websocketInput.value = localStorage.getItem('websocketInput') || 'localhost'
submitButton.onclick = () => {
  submitButton.disabled = true
  if (socket) socket.close();
  webSocket(`ws://${websocketInput.value}:8080`)
  localStorage.setItem('websocketInput', websocketInput.value);

  $('#qrcode').qrcode({
    text: `ws://${websocketInput.value}:8080`,
    width: 100,
    height: 100,
    background: '#fff',
    foreground: '#000'
  });
  $('.button-group').css('display', 'none');
  $('.qr-group').css('display', 'block');
  $('#image').css('display', 'block');
};
