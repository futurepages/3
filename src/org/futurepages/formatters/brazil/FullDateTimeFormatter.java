package org.futurepages.formatters.brazil;

import java.util.Calendar;
import java.util.Locale;
import org.futurepages.core.exception.AppLogger;
import org.futurepages.core.formatter.AbstractFormatter;
import org.futurepages.util.brazil.enums.MonthEnum;
import org.futurepages.util.brazil.NumberUtil;

/**
 * Formata a data no formato DD/MM/YYYY
 */
public class FullDateTimeFormatter extends AbstractFormatter<Calendar> {

	private boolean dateAndTime = true;
	private boolean extense = false;

	public FullDateTimeFormatter(boolean dateAndTime) {
		this.dateAndTime = dateAndTime;
	}

	public FullDateTimeFormatter(boolean dateAndTime, boolean extense) {
		this.dateAndTime = dateAndTime;
		this.extense = extense;
	}

	public String format(Calendar cal, Locale loc) {
		return valueOf(cal);
	}

	public String valueOf(Calendar cal){
		try {
			StringBuilder sb = new StringBuilder();
			int dia = cal.get(Calendar.DAY_OF_MONTH);
			if(dia==1){
				sb.append("primeiro");
				if(extense){
					sb.append(" dia");
				}
			}else{
				sb.append(NumberUtil.numeroPorExteso(dia));
				if(extense){
					sb.append(" dias");
				}
			}
			if(extense){
				sb.append(" do mês");
			}
			sb.append(" de ").append(MonthEnum.get(cal));
			if(extense){
				sb.append(" do ano");
			}
			sb.append(" de ");
			sb.append(NumberUtil.numeroPorExteso(cal.get(Calendar.YEAR)));


			if (dateAndTime) {
				int hora = cal.get(Calendar.HOUR_OF_DAY);
				int min = cal.get(Calendar.MINUTE);
				if (hora > 1) {
					sb.append(" às ");
				} else {
					sb.append(" à ");
				}
				if (hora == 1) {
					sb.append("uma");
				} else if (hora == 2) {
					sb.append("duas");
				} else if (hora == 21) {
					sb.append("vinte e uma");
				} else if (hora == 22) {
					sb.append("vinte e duas");
				} else {
					sb.append(NumberUtil.numeroPorExteso(hora));
				}
				if (hora > 1) {
					sb.append(" horas");
				} else {
					sb.append(" hora");
				}
				if (min > 0) {
					sb.append(" e ");
					sb.append(NumberUtil.numeroPorExteso(min));
					if (min != 1) {
						sb.append(" minutos");
					} else {
						sb.append(" minuto");
					}
				}
			}

			return sb.toString();
		} catch (Exception ex) {
			AppLogger.getInstance().execute(ex);
			return "";
		}
	}
}
