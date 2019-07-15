const path = require('path');
const slsw = require('serverless-webpack');

module.exports = {
  mode: slsw.lib.webpack.isLocal ? 'development' : 'production',
  entry: slsw.lib.entries,
  devtool: 'source-map',
  resolve: {
    extensions: ['.js', '.jsx', '.json', '.ts', '.tsx'],
  },
  output: {
    libraryTarget: 'commonjs',
    path: path.join(__dirname, '.webpack'),
    filename: '[name].js',
  },
  target: 'node',
  externals: {
    // WARNING in ./node_modules/retry-request/index.js
    // Module not found: Error: Can't resolve 'request' in 'node_modules/retry-request'
    "request": "request",
    // WARNING in ./node_modules/express/lib/view.js 81:13-25
    // Critical dependency: the request of a dependency is an expression
    "express": "express"
  },
  module: {
    rules: [
      // all files with a `.ts` or `.tsx` extension will be handled by `ts-loader`
      {
        test: /\.tsx?$/,
        loader: 'ts-loader'
      },
    ],
  },
};