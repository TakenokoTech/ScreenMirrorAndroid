var os = require('os');

export function getLocalAddress() {
    const ifacesObj = { ipv4: [], ipv6: [] };
    for (let dev in os.networkInterfaces()) {
        os.networkInterfaces()[dev].forEach(details => {
            if (!details.internal) {
                switch (details.family) {
                    case 'IPv4':
                        ifacesObj.ipv4.push({ name: dev, address: details.address });
                        break;
                    case 'IPv6':
                        ifacesObj.ipv6.push({ name: dev, address: details.address });
                        break;
                }
            }
        });
    }
    return ifacesObj;
}
