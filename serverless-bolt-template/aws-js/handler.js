'use strict';

const app = require('./app');
module.exports.app = require('serverless-http')(app.expressApp);
