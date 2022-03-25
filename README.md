# Asteroids_API
API REST hecha con Spring boot que se encarga de obtener los 3 asteroides mas peligrosos para la Tierra segun la NASA, en funcion de los dias indicados por parametro

Para poder trabajar con la API, debido a que trabajamos con una API externa, debemos descargarnos un certificado. Para ello, hay que accerder a https://api.nasa.gov/ y exportar el certificado con extension ".cer"(sin comillas)

Una vez extraido el certificado, debemos guardarlo en nuestro alamcen de certificados de Java, para ello, copiaremos el certificado que hemos exportado a la ruta de nuestro almacen.

Por regla general, esta suele ser parecida a lo siguiente: C:\Program Files (x86)\Java\jre1.6.0_22\lib\security\

Una vez tengamos el certificado en el directorio, mediante CMD(Consola de Comandos), en la ruta de nuestro almacen, ejecutamos el siguiente comando, sustituyendo alla donde corresponda:

keytool -import -alias example -keystore  "C:\Program Files (x86)\Java\jre1.6.0_22\lib\security\cacerts" -file example.cer

Al reiniciar la aplicacion, ya funcionara todo correctamente.

Si no queremos tener limitaciones a la hora de probar la API externa, podemos acceder a https://api.nasa.gov/ y rellenar el formulario. Tras rellenarlo, nos devolvera una API_KEY, la cual deberemos de sustituir en la URI, justo donde pone DEMO_KEY:

"&api_key=DEMO_KEY"
