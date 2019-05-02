import 'source-map-support/register';
import { app } from './app';
// FIXME: currently accessing private fields
const expressApp = (app as any).receiver.app;
export const dispatcher = require('serverless-http')(expressApp);