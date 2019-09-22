import express, { NextFunction, Request, Response } from 'express';

export async function GetAdress(req: Request, res: Response, next: NextFunction) {
    const result = { ready: false };

    Log.info('200.\n', result);
    res.status(200).json(result);
}
