CREATE TABLE IF NOT EXISTS `mensajes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `src_usr` varchar(100) NOT NULL,
  `dst_sala` varchar(100) NOT NULL,
  `type` varchar(10) NOT NULL,
  `text` varchar(500) DEFAULT NULL,
  `timestamp` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_usuario` varchar(40) NOT NULL,
  `current_room_id` varchar(40) DEFAULT NULL,
  `is_root` boolean NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre_usuario` (`nombre_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `salas` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_sala` int(11) NOT NULL,
  `nombre_sala` varchar(100) NOT NULL,
  `creator` varchar(40) NOT NULL,
  `private` tinyint(1) NOT NULL,
  `allowed` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_sala` (`id_sala`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `mensajes_censurados` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `src_usr` varchar(100) NOT NULL,
  `dst_sala` varchar(100) NOT NULL,
  `text` varchar(500) DEFAULT NULL,
  `censored_words` varchar(1000) NOT NULL,
  `timestamp` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;