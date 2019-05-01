import 'source-map-support/register';
import { app } from './app';
export const dispatcher = require('serverless-http')(app);