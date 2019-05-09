import 'source-map-support/register';
import { expressApp } from './app';
export const dispatcher = require('serverless-http')(expressApp);