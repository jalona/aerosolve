package com.airbnb.aerosolve.training.pipeline

import org.apache.spark.{SparkContext, SparkConf}
import org.slf4j.{LoggerFactory, Logger}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

object JobRunner {
  def main(args: Array[String]): Unit = {
    val log: Logger = LoggerFactory.getLogger("Job.Runner")

  if (args.length < 1) {
      log.error("Usage: Job.Runner config_name job1,job2...")
      System.exit(-1)
    }

    log.info("Loading config from " + args(0))

    val config = ConfigFactory.load(args(0))

    val jobs : Seq[String] = if (args.length == 1) {
      config.getStringList("jobs")
    } else {
      args(1).split(',')
    }

    val name = try {
      config.getString("job_name")
    } catch {
      case e : Exception => "JobRunner"
    }

    val conf = new SparkConf().setAppName(name)
    val sc = new SparkContext(conf)

    for (job <- jobs) {
      log.info("Running " + job)
      try {
        job match {
          case "GenericMakeExamples" => GenericPipeline
            .makeTrainingRun(sc, config)
          case "GenericDebugExamples" => GenericPipeline
            .debugExampleRun(sc, config)
          case "GenericDebugTransforms" => GenericPipeline
            .debugTransformsRun(sc, config)
          case "GenericDebugScores" => GenericPipeline
            .debugScoreTableRun(sc, config)
          case "GenericTrainModel" => GenericPipeline
            .trainingRun(sc, config)
          case "GenericEvalModel" => GenericPipeline
            .evalRun(sc, config, "eval_model")
          case "GenericCalibrateModel" => GenericPipeline
            .calibrateRun(sc, config)
          case "GenericEvalModelCalibrated" => GenericPipeline
            .evalRun(sc, config, "eval_model_calibrated")
          case "GenericDumpModel" => GenericPipeline
            .dumpModelRun(sc, config)
          case "GenericDumpForest" => GenericPipeline
            .dumpForestRun(sc, config)
          case "GenericDumpFullRankLinearModel" => GenericPipeline
            .dumpFullRankLinearRun(sc, config)
          case "GenericScoreTable" => GenericPipeline
            .scoreTableRun(sc, config)
          case "GenericParamSearch" => GenericPipeline
            .paramSearch(sc, config)
          case _ => log.error("Unknown job " + job)
        }
      } catch {
        case e : Exception => log.error("Exception on job %s : %s".format(job, e.toString))
          System.exit(-1)
      }
    }

    log.info("Job(s) finished successfully")
    sc.stop()

    System.exit(0)
  }
}
