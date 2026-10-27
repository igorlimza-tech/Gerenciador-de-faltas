CREATE DATABASE  IF NOT EXISTS `sistemadefaltas` /*!40100 DEFAULT CHARACTER SET utf8mb3 COLLATE utf8mb3_bin */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `sistemadefaltas`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sistemadefaltas
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `grade`
--

DROP TABLE IF EXISTS `grade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grade` (
  `idGRADE` int NOT NULL AUTO_INCREMENT,
  `turma_id` int NOT NULL,
  `dia_semana` varchar(20) COLLATE utf8mb3_bin NOT NULL,
  `peso_aula` int NOT NULL,
  `turma_idTURMA` int NOT NULL,
  PRIMARY KEY (`idGRADE`),
  KEY `fk_GRADE_TURMA1_idx` (`turma_idTURMA`),
  CONSTRAINT `fk_grade_turma1` FOREIGN KEY (`turma_idTURMA`) REFERENCES `turma` (`idTURMA`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `matricula`
--

DROP TABLE IF EXISTS `matricula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `matricula` (
  `idMATRICULA` int NOT NULL AUTO_INCREMENT,
  `aluno_id` int NOT NULL,
  `ra_aluno` varchar(50) COLLATE utf8mb3_bin NOT NULL,
  `turma_id` int NOT NULL,
  `email_institucional` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `usuarios_idUSUARIOS` int NOT NULL,
  `turma_idTURMA` int NOT NULL,
  PRIMARY KEY (`idMATRICULA`),
  UNIQUE KEY `ra_aluno_UNIQUE` (`ra_aluno`),
  UNIQUE KEY `email_institucional_UNIQUE` (`email_institucional`),
  KEY `fk_MATRICULA_TURMA1_idx` (`turma_idTURMA`),
  KEY `fk_MATRICULA_USUARIOS1_idx` (`usuarios_idUSUARIOS`),
  CONSTRAINT `fk_matricula_turma1` FOREIGN KEY (`turma_idTURMA`) REFERENCES `turma` (`idTURMA`),
  CONSTRAINT `fk_matricula_usuarios1` FOREIGN KEY (`usuarios_idUSUARIOS`) REFERENCES `usuarios` (`idUSUARIOS`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `presença`
--

DROP TABLE IF EXISTS `presença`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presença` (
  `idPRESENÇA` int NOT NULL AUTO_INCREMENT,
  `registro_id` int NOT NULL,
  `aluno_id` int NOT NULL,
  `status_presenca` varchar(20) COLLATE utf8mb3_bin NOT NULL,
  `turma_idTURMA` int NOT NULL,
  `registros_idREGISTROS` int NOT NULL,
  PRIMARY KEY (`idPRESENÇA`),
  KEY `fk_PRESENÇA_REGISTROS1_idx` (`registros_idREGISTROS`),
  KEY `fk_PRESENÇA_TURMA1_idx` (`turma_idTURMA`),
  CONSTRAINT `fk_presença_registros1` FOREIGN KEY (`registros_idREGISTROS`) REFERENCES `registros` (`idREGISTROS`),
  CONSTRAINT `fk_presença_turma1` FOREIGN KEY (`turma_idTURMA`) REFERENCES `turma` (`idTURMA`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registros`
--

DROP TABLE IF EXISTS `registros`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registros` (
  `idREGISTROS` int NOT NULL AUTO_INCREMENT,
  `turma_id` int NOT NULL,
  `data_aula` date NOT NULL,
  `quantidade_aula` int NOT NULL DEFAULT '1',
  `numero_chamada` int NOT NULL,
  `turma_idTURMA` int NOT NULL,
  PRIMARY KEY (`idREGISTROS`),
  KEY `fk_REGISTROS_TURMA1_idx` (`turma_idTURMA`),
  CONSTRAINT `fk_registros_turma1` FOREIGN KEY (`turma_idTURMA`) REFERENCES `turma` (`idTURMA`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `turma`
--

DROP TABLE IF EXISTS `turma`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `turma` (
  `idTURMA` int NOT NULL AUTO_INCREMENT,
  `professor_id` int NOT NULL,
  `registro_funcional` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
  `email_institucional` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `instituicao` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `nome_curso` varchar(50) COLLATE utf8mb3_bin NOT NULL,
  `nome_turma` varchar(50) COLLATE utf8mb3_bin NOT NULL,
  `nome_materia` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `carga_horaria` int NOT NULL,
  `codigo_acesso` varchar(6) COLLATE utf8mb3_bin NOT NULL,
  `periodo` varchar(20) COLLATE utf8mb3_bin NOT NULL,
  `limites_faltas` decimal(5,2) NOT NULL DEFAULT '25.00',
  `usuarios_idUSUARIOS` int NOT NULL,
  PRIMARY KEY (`idTURMA`),
  UNIQUE KEY `codigo_acesso_UNIQUE` (`codigo_acesso`),
  UNIQUE KEY `email_institucional_p_UNIQUE` (`email_institucional`),
  KEY `fk_turma_usuarios1_idx` (`usuarios_idUSUARIOS`),
  CONSTRAINT `fk_turma_usuarios1` FOREIGN KEY (`usuarios_idUSUARIOS`) REFERENCES `usuarios` (`idUSUARIOS`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `idUSUARIOS` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `email` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `senha` varchar(100) COLLATE utf8mb3_bin NOT NULL,
  `perfil` enum('PROFESSOR','ALUNO') COLLATE utf8mb3_bin NOT NULL,
  PRIMARY KEY (`idUSUARIOS`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-26 22:46:51
