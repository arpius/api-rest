<?php
	require '../vendor/autoload.php';

	# Creamos la aplicación
	$app = new \Slim\Slim();
	
	# Configuramos la app
	$app->config(
		array(
			'debug' => true
		)
	);

	# Tipo de contenido y codificación
	$app->contentType('text/html; charset=utf-8');

	# Configuración BBDD
	define('BD_SERVIDOR', 'localhost');
	define('BD_NOMBRE', 'api-rest');
	define('BD_USUARIO', 'root');
	define('BD_CLAVE', '');

	# Conexión BBDD
	$db = new PDO('mysql:host=' .BD_SERVIDOR. ';dbname=' .BD_NOMBRE. ';', BD_USUARIO, BD_CLAVE);
	$db->exec('set names utf8');

	# Rutas de la app
	# Mostramos la pantalla principal
	$app->get('/', function() {
		echo "Trasteando con Slim para crear una API REST";
	});

	# Mostramos todos los usuarios
	$app->get('/usuarios', function() use($db) {
		# Ejecutamos la consulta
		$consulta = $db->prepare("select * from usuarios");
		$consulta->execute();

		# Guardamos los resultados en un array asociativo
		$resultados = $consulta->fetchAll(PDO::FETCH_ASSOC);

		# Devolvemos el array como json
		echo json_encode($resultados);
	});

	# Mostramos un usuario concreto mediante su id
	$app->get('/usuarios/:id', function($idusuario) use($db) {
		$consulta = $db->prepare("select * from usuarios where id=:param1");

		# Asociamos param1 con el valor que toque
		$consulta->execute(
			array(
				':param1' => $idusuario
			)
		);

		$resultados = $consulta->fetchAll(PDO::FETCH_ASSOC);

		echo json_encode($resultados);
	});

	# Creamos un nuevo usuario
	$app->post('/usuarios', function() use($db, $app) {
		$datosform = $app->request;

		# Como el id es autonumérico y el alta coge la fecha actual, solo pedimos nombre y correo
		$consulta = $db->prepare("insert into usuarios(nombre, email) values(:nombre, :email)");

		$estado = $consulta->execute(
			array(
				':nombre' => $datosform->post('nombre'),
				':email' => $datosform->post('email')
			)
		);

		if($estado) 
			echo json_encode(array('estado' => true, 'mensaje' => 'Datos insertados correctamente.'));
		else 
			echo json_encode(array('estado' => false, 'mensaje' => 'Error al insertar los datos.'));
	});

	# Borramos un usuario
	$app->delete('/usuarios/:id', function($idusuario) use($db) {
		$consulta = $db->prepare("delete from usuarios where id=:id");
		$consulta->execute(
			array(
				':id' => $idusuario
			)
		);

		if($consulta->rowCount() == 1) 
			echo json_encode(array('estado' => true, 'mensaje' => 'Usuario (' .$idusuario. ') borrado correctamente.'));
		else
			echo json_encode(array('estado' => false, 'mensaje' => 'Error al borrar usuario.'));
	});

	# Actualizamos un usuario concreto
	$app->put('/usuarios/:id', function($idusuario) use($db, $app) {
		$datosform = $app->request;

		$consulta = $db->prepare("update usuarios set nombre=:nombre, email=:email, created_at=:alta where id=:id");

		$estado = $consulta->execute(
			array(
				':id' => $idusuario,
				':nombre' => $datosform->post('nombre'),
				':email' => $datosform->post('email'),
				':alta' => $datosform->post('alta')
			)
		);

		if($consulta->rowCount() == 1) 
			echo json_encode(array('estado' => true, 'mensaje' => 'Datos actualizados correctamente.'));
		else 
			echo json_encode(array('estado' => false, 'mensaje' => 'Error al actualizar los datos.'));

	});

	# Ejecutamos la app
	$app->run();
?>
