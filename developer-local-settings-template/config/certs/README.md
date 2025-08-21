Keys/certificates for local development

See global [README.md](../../../README.md) for more information.



add to host file:
   ```
127.0.0.1 local-esc-bs.localhost
   ```

Install mkcert
Make sure to install the mkcert CA to you system (browser, java keystore etc.) by running
 
   ```
   mkcert -install
   ```

   
  ```
  # make sure you are at the right location e.g. 
  cd  developer-local-settings/config
  mkcert -key-file certs/local-esc-bs.localhost.key -cert-file certs/local-esc-bs.localhost.pem local-esc-bs.localhost
  ```
