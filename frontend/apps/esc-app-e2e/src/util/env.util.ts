import dotenv from 'dotenv';
import * as path from 'node:path';

// Read from ".env" file.
dotenv.config({path: path.resolve(__dirname, '.env')});

export const testUsers = {
  ESC_ADMIN: {
    name: process.env['ESC_ADMIN_USER'],
    password: process.env['ESC_ADMIN_PASSWORD'],
  },
  ESC_EMPFANG: {
    name: process.env['ESC_EMPFANG_USER'],
    password: process.env['ESC_EMPFANG_PASSWORD'],
  },
  ESC_MPA: {
    name: process.env['ESC_MPA_USER'],
    password: process.env['ESC_MPA_PASSWORD'],
  },
  ESC_ORTHOPOSTIN: {
    name: process.env['ESC_ORTHOPISTIN_USER'],
    password: process.env['ESC_ORTHOPISTIN_PASSWORD'],
  },
  ESC_SCHULAERZTIN: {
    name: process.env['ESC_SCHULAERZTIN_USER'],
    password: process.env['ESC_SCHULAERZTIN_PASSWORD'],
  },
  ESC_SUPERMPA: {
    name: process.env['ESC_SUPERMPA_USER'],
    password: process.env['ESC_SUPERMPA_PASSWORD'],
  },
  ESC_SUPPORT: {
    name: process.env['ESC_SUPPORT_USER'],
    password: process.env['ESC_SUPPORT_PASSWORD'],
  },

};

export const baseURL = process.env['BASE_URL'];
