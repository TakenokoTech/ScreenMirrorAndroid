import express, { NextFunction, Request, Response } from 'express';
import { Log } from '../../utils/Log';

import { getLocalAddress } from '../../utils/Network';

export async function GetAdress(req: Request, res: Response, next: NextFunction) {
    const result = getLocalAddress();
    Log.info('200.\n', `ipv4: ${result.ipv4.length}, ipv6: ${result.ipv6.length}`);
    res.status(200).json(result);
}
