import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

import WebSocketClient from './view/WebsocketClient';
import GrpcClient from './view/GrpcClient';

/*==================================================================================*/
const image = document.getElementById('image');
const submitButton = document.getElementById('submit-button');
const websocketInput = document.getElementById('websocketInput');
websocketInput.value = localStorage.getItem('websocketInput') || 'localhost';
/*==================================================================================*/

(async () => {
    chnageSubmitButton(false);
    const address = await fetch('http://localhost:3000/address');
    const ipv4 = (await address.json())['ipv4'];
    $('#address-group').empty();
    ipv4.forEach((a, i) => {
        const id = `address-${i}`;
        $('#address-group').append(`<div id="${id}" class="badge badge-light">${a.address}: ${a.name}</div>`);
        $(`#${id}`).click(() => {
            websocketInput.value = a.address;
        });
    });
})();

function chnageSubmitButton(close) {
    if (close) {
        submitButton.disabled = false;
        // $('#submit-button').css('display', 'none');
        $('#submit-button').addClass('close-button');
        $('#submit-button').removeClass('open-button');
        $('#submit-button').html('Close');
        submitButton.onclick = () => {
            $('#qrcode').empty();
            submitButton.disabled = true;
            WebSocketClient.close();
            GrpcClient.close();
            $('.qr-group').css('display', 'none');
            $('#image').css('display', 'none');
            chnageSubmitButton(false);
        };
    } else {
        submitButton.disabled = false;
        $('#submit-button').removeClass('close-button');
        $('#submit-button').addClass('open-button');
        $('#submit-button').html('Connect');
        submitButton.onclick = () => {
            $('#qrcode').empty();
            submitButton.disabled = true;
            WebSocketClient.open(`ws://${websocketInput.value}:8080`);
            GrpcClient.open();
            localStorage.setItem('websocketInput', websocketInput.value);
            $('.qr-group').css('display', 'block');
            $('#image').css('display', 'block');
            chnageSubmitButton(true);
        };
    }
}
