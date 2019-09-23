import log4js from 'log4js';

log4js.configure({
    appenders: {
        system: { type: 'console', filename: `${__dirname}/system.log` },
        access: { type: 'console', filename: `${__dirname}/access.log` },
    },
    categories: {
        default: { appenders: ['system'], level: 'debug' },
        web: { appenders: ['access'], level: 'debug' },
    },
});

const systemLogger = log4js.getLogger('default');
const accessLogger = log4js.getLogger('web');

class log {
    debug(message: any, ...args: any[]) {
        systemLogger.debug(message, ...args);
    }
    info(message: any, ...args: any[]) {
        systemLogger.info(message, ...args);
    }
    warn(message: any, ...args: any[]) {
        systemLogger.warn(message, ...args);
    }
    error(message: any, ...args: any[]) {
        systemLogger.error(message, ...args);
    }
}

export const connectLogger = log4js.connectLogger(accessLogger, {});
export const Log = new log();

console.log('');
