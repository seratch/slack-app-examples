'use strict';

import { expressApp } from './app';
export const app = require('serverless-http')(expressApp);
