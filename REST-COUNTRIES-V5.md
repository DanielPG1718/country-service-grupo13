# Country Service con REST Countries v5

Documentacion oficial: https://restcountries.com/docs/countries

## IntelliJ

1. Obten una API key propia en https://restcountries.com/sign-up.
2. Run > Edit Configurations > CountryServiceApplication > Modify options > Environment variables.
3. Agrega RESTCOUNTRIES_API_KEY como nombre y tu clave como valor.
4. Conserva las variables de Eureka que utilices para conectar IntelliJ con Docker.
5. Con Config y Discovery iniciados, ejecuta CountryServiceApplication.

No guardes la clave en archivos versionados. RESTCOUNTRIES_BASE_URL es opcional;
el valor predeterminado es https://api.restcountries.com/countries/v5.

Se usan las propiedades restcountries.v5.base-url y restcountries.v5.api-key.
La propiedad antigua restcountries.api.base-url del Config Server ya no se usa,
porque apunta a v3.1. Si centralizas la nueva configuracion, usa las propiedades v5
y el placeholder ${RESTCOUNTRIES_API_KEY:}, nunca una clave real.

## Pruebas desde IntelliJ

Con el puerto 8084 configurado por Config Server:

- http://localhost:8084/countries/PE
- http://localhost:8084/countries/PER
- http://localhost:8084/countries/search?name=peru
- http://localhost:8084/countries/region/Americas
- http://localhost:8084/countries/all

Se conserva la respuesta del frontend: name, capital, region y flag (URL PNG).
El cliente recorre las paginas de v5 para devolver la lista completa.
Los codigos pueden ser ISO alpha-2, alpha-3 o numericos de tres digitos.

La clave publica rc_live_demo devuelve una muestra fija, incluso para consultas
distintas. El servicio la detecta y responde 503 para no presentar resultados falsos.
Una clave ausente, rechazada o con cuota agotada tambien produce un error claro.
Un fallo del proveedor produce 502; una busqueda sin resultados produce 404.

## Verificacion automatizada

Ejecuta .\mvnw.cmd test con JDK 25. Las pruebas no requieren una API key real,
Config Server ni Eureka, y no consumen cuota del proveedor.
