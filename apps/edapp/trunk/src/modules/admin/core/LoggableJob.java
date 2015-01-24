package modules.admin.core;

import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import modules.admin.beans.Log;
import modules.admin.enums.LogType;
import org.futurepages.core.exception.DefaultExceptionLogger;
import org.futurepages.core.persistence.Dao;
import org.futurepages.util.DateUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class LoggableJob implements Job {

	public abstract void run() throws Exception;

	protected long tempoDeInicio;
	protected long tempoDeFinalizacao;
	protected HashMap<String, String> mapa = new HashMap<String, String>();

	protected void executaAntes() {
		System.out.println(DateUtil.viewDateTime(Calendar.getInstance(), "dd/MM/yyyy - HH:mm:ss") + ": Executing Job >>> " + this.getClass().getSimpleName());
	}

	protected void executaDepois() {
	}

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		String obs = null;
		Calendar agora = Calendar.getInstance();

		executaAntes();

		tempoDeInicio = System.currentTimeMillis();
		try {
			run();
		} catch (Exception ex) {
			DefaultExceptionLogger.getInstance().execute(ex);
			obs = ex.getMessage();
		}
		tempoDeFinalizacao = System.currentTimeMillis();

		executaDepois();

		Log log = new Log();

		log.setDateTime(agora);
		log.setIdName("timeMs");
		log.setIdValue(String.valueOf(tempoDeFinalizacao - tempoDeInicio));
		log.setClassName(this.getClass().getName());
		log.setLogType(LogType.SYSTEM);
		log.setLogContent(Log.toLog(mapa));
		log.setObs(obs);


		Dao.beginTransaction();
		Dao.save(log);
		Dao.commitTransaction();
		Dao.close();
	}

	protected void log(String value) {
		log(value, null);
	}

	protected void log(String value, Level level) {
		if (level == null) {
			level = Level.INFO;
		}
		Logger.getLogger(this.getClass().getName()).log(level, value);

	}
}