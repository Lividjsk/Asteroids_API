package com.atmira.asteroids.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.atmira.asteroids.pojo.Asteroids;

@RestController
@RequestMapping("/")
@ResponseBody
public class AsteroidsController {

	// URI API NASA
	private static final String uriNASA = "https://api.nasa.gov/neo/rest/v1/feed?start_date=#startDate#&end_date=#endDate#&api_key=i4m5qpFs8Nr3vzXPb70C116KaEXjh7XGvJPlhmsH";

	// DateFormat para las fechas
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Obtener los 3 asteroides mas grandes con riesgo de impacto en la Tierra
	 * 
	 * @method GET
	 * @param days Numero de dias entre los que tenemos que comparar
	 * @return Listado con los 3 asteroides mas grandes
	 */
	@GetMapping("/asteroids")
	public List<Asteroids> getAsteroids(@RequestParam String days) {

		// Declaramos el objeto de respuesta de la peticion
		List<Asteroids> listAsteroids = null;

		try {

			// Convertimos la variable de entrada a un entero
			Integer dias = Integer.parseInt(days);

			// Declaramos el objeto para obtener un intervalo de fechas en formato String
			List<String> intervaloFechas = new ArrayList<String>();

			// 1. Verificamos que el parametro days se ha recibido y viene informado
			if (dias == null || dias < 1 || dias > 7) {
				throw new RuntimeException("El parametro days debe ser informado y debe estar comprendido entre 1 y 7");
			}

			// 2. Obtenemos la fecha actual y la convertimos al formato de fecha necesario
			// para atentar contra la API de la NASA
			Date fechaActual = new Date();

			// Formateamos la fecha actual al formato necesario
			String fechaInicio = format.format(fechaActual);

			// Para calcular la fecha de fin, sumamos en milisegundos a la fecha actual la
			// cantidad de milisegundos de los dias
			// indicados por el parametro days
			Date fechaCalculada = new Date(fechaActual.getTime() + (1000 * 60 * 60 * 24 * dias));

			// Calculamos la fecha de fin, en funcion del parametro days
			String fechaFin = format.format(fechaCalculada);

			// Mas adelante, nos resultada util tener el intervalo de fechas, desde la de
			// inicio hasta la de fin
			intervaloFechas.add(fechaInicio);
			for (int i = 1; i < dias; i++) {
				Date fechaAuxiliar = new Date(fechaActual.getTime() + (1000 * 60 * 60 * 24 * i));
				String fechaFormateada = format.format(fechaAuxiliar);
				intervaloFechas.add(fechaFormateada);
			}
			intervaloFechas.add(fechaFin);

			// Una vez tenemos calculadas las fechas, las reemplazamos en la URI de la API
			// de la NASA
			String uri = uriNASA.replace("#startDate#", fechaInicio);
			uri = uri.replace("#endDate#", fechaFin);

			// Finalmente, atentamos contra la API de la NASA
			RestTemplate template = new RestTemplate();

			// Recuperamos el objeto en formato String
			String response = template.getForObject(uri, String.class);

			// Ahora pasamos la respuesta de la peticion a formato JSON
			JSONObject asteroids = new JSONObject(response);

			// Una vez tenemos el listado de los asteroides, los procesamos para obtener el
			// listado de los 3 mas grandes y proximos
			listAsteroids = calculateAsteroids(asteroids, intervaloFechas);
		} catch (Exception e) {
			throw new RuntimeException("Hubo un error al procesar la peticion: " + e.getMessage());
		}

		// Devolvemos la respuesta
		return listAsteroids;
	}

	/**
	 * Funcion que obtiene el listado con los 3 asteroides mas grandes y peligrosos para el planeta Tierra
	 * @param objects Respuesta de la API de la NASA en formato JSON
	 * @param intervaloFechas Listado con las fechas indicadas, segun la peticion
	 * @return Listado con los 3 asteroides mas peligrosos para el planeta Tierra
	 */
	private List<Asteroids> calculateAsteroids(JSONObject objects, List<String> intervaloFechas) {

		// Declaramos el objeto que vamos a devolver tras procesar la respuesta de la
		// API
		// Utilizamos un HashSet para evitar elementos duplicados
		HashSet<Asteroids> asteroids = new HashSet<Asteroids>(0);
		List<Asteroids> result = null;

		try {

			// Para el calculo, tenemos que ir obteniendo los asteroides recuperados en la
			// peticion por fecha
			JSONObject asteroidesCercanos = objects.getJSONObject("near_earth_objects");

			// Ahora, en funcion de la fecha, vamos a ir obteniendo los asteroides de mayor
			// tamaño
			for (String fecha : intervaloFechas) {
				JSONArray array = asteroidesCercanos.getJSONArray(fecha);
				for (int j = 0; j < array.length(); j++) {
					JSONObject asteroide = array.getJSONObject(j);
					// Si el asteroide es potencialmente peligroso, continuamos
					if (asteroide.getBoolean("is_potentially_hazardous_asteroid")) {

						// Obtenemos el tamaño minimo y maximo en kilometros
						JSONObject estimatedDiameter = asteroide.getJSONObject("estimated_diameter");

						// Vamos avanzando hacia los objetos internos del JSON hasta obtener el tamaño
						// minimo y maximo
						// para calcular la media
						JSONObject kilometers = estimatedDiameter.getJSONObject("kilometers");

						// Tamaño minimo en kilometros
						Double diameterMin = kilometers.getDouble("estimated_diameter_min");

						// Tamaño maximo en kilometros
						Double diameterMax = kilometers.getDouble("estimated_diameter_max");

						// Calculamos el tamaño medio
						Double diameter = calculateDiameter(diameterMin, diameterMax);

						// Al utilizar un HashSet, no nos va a dejar introducir elementos repetidos
						// por tanto insertamos todos los elementos que se procesan
						// Primero vamos a obtener los campos restantes: Velocidad, fecha y planeta
						JSONArray closeApproachData = asteroide.getJSONArray("close_approach_data");

						// Vamos avanzando hacia los objetos internos del JSON hasta obtener el resto de
						// campos
						// necesarios a devolver en la respuesta
						JSONObject object = closeApproachData.getJSONObject(0);

						// Objeto que contiene la velocidad del asteroide en kilometros
						JSONObject relative_velocity = object.getJSONObject(("relative_velocity"));

						// Velocidad del asteroide en kilometros
						Double velocity = relative_velocity.getDouble("kilometers_per_hour");

						// Fecha en la que se tuvo constancia del asteroide
						Date fechaAst = format.parse(object.getString("close_approach_date"));

						// Planeta sobre el que orbita
						String planet = object.getString("orbiting_body");

						// Creamos un objeto asteroide con todos los valores anteriores
						Asteroids element = new Asteroids(asteroide.getString("name"), diameter, velocity, fechaAst,
								planet, false);

						// Guardamos en la lista solo los asteroides potencialmente peligrosos
						asteroids.add(element);
					}
				}
			}

			// Una vez tenemos todos los elementos, tenemos que obtener aquellos cuyo
			// diametro sea mayor
			// Para ello vamos a recorrer el HashSet anterior para obtener los 3 con mayor
			// diametro
			int contador = 0;
			int position = 0;
			double diameter = 0.0;
			result = new ArrayList<Asteroids>(0);

			// Creamos un array booleanos con valor falso por defecto, con el mismo tamaño
			// que el numero de asteroides de nuestra lista
			// Esto lo hacemos asi para evitar meter en la respuesta dos veces un mismo
			// asteroide
			Boolean[] markeds = new Boolean[asteroids.size()];

			// Rellenamos el array de booleanos con false
			for (int m = 0; m < asteroids.size(); m++) {
				markeds[m] = false;
			}

			// Vamos a recorrer el HashSet hasta tener los 3 asteroides
			//while ((asteroids.size() > 3 && contador < 3) || (asteroids.size() < 3 && contador < asteroids.size())) {
			if(asteroids.size() < 3) {
				
				while(result.size() < asteroids.size()) {
					// Lo convertimos a array para recorrerlo de la forma en que necesitamos
					Asteroids[] arrayAst = asteroids.toArray(new Asteroids[asteroids.size()]);
	
					// En este bucle vamos comprobando cual asteroide tiene el mayor diametro
					// Y sino ha sido seleccionado previamente
					for (int n = 0; n < arrayAst.length; n++) {
						if (arrayAst[n].getDiameter() > diameter && !markeds[n]) {
							diameter = arrayAst[n].getDiameter();
							position = n;
						}
					}
	
					// Cuando terminamos el bucle, insertamos en el array de resultados
					result.add(new Asteroids(arrayAst[position]));
					// Marcamos el asteroide como insertado en el objeto de respuesta
					markeds[position] = true;
	
					// Reiniciamos los parametros
					position = 0;
					diameter = 0.0;
				}
			}else {
				while (contador < 3) {
	
					// Lo convertimos a array para recorrerlo de la forma en que necesitamos
					Asteroids[] arrayAst = asteroids.toArray(new Asteroids[asteroids.size()]);
	
					// En este bucle vamos comprobando cual asteroide tiene el mayor diametro
					// Y sino ha sido seleccionado previamente
					for (int n = 0; n < arrayAst.length; n++) {
						if (arrayAst[n].getDiameter() > diameter && !markeds[n]) {
							diameter = arrayAst[n].getDiameter();
							position = n;
						}
					}
	
					// Cuando terminamos el bucle, insertamos en el array de resultados
					result.add(new Asteroids(arrayAst[position]));
					// Marcamos el asteroide como insertado en el objeto de respuesta
					markeds[position] = true;
	
					// Ahora, eliminamos del conjunto el elemento en la posicion
					// Solo se hace en caso de que haya mas de 3 elementos en el conjunto
					asteroids.remove(arrayAst[position]);
	
					// Reiniciamos los parametros
					position = 0;
					diameter = 0.0;
					contador++;
				}
			}

		} catch (JSONException | ParseException e) {
			throw new RuntimeException("Ocurrio un error al leer los datos de la NASA");
		}

		// Devolvemos el listado con los 3 asteroides mas grandes
		return result;
	}

	/**
	 * Calculo del tamaño medio del asteroide
	 * @param min Tamaño minimo del asteroide
	 * @param max Tamaño maximo del asteroide
	 * @return Tamaño medio del asteroide
	 */
	private Double calculateDiameter(Double min, Double max) {
		
		// Declaramos la variable en la que vamos a devolver el tamaño medio del asteroide
		Double diameter = 0.0;

		// Calculamos el tamaño medio del asteroide
		diameter = (min + max) / 2;

		// Devolvemos el tamaño medio del asteroide
		return diameter;
	}

}
