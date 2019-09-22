import cors from 'cors';
import express, { NextFunction, Request, Response } from 'express';

import { connectLogger, Log } from '../utils/Log';
import { GetAdress } from './express/GetAdress';

// import { getLocalAddress } from '../utils/Network';
// let localAddress = getLocalAddress();

const app = express();
app.use(connectLogger);
app.use('*', cors());
app.use((req, res, next) => {
    Log.debug(`call. ${req.url}`);
    next();
});
app.get('/address', GetAdress);

export function startExpress() {
    app.listen(3000, () => Log.info('Listening on port 3000!'));
}
