package de.upb.manuel;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TableRowData implements Cloneable, Comparable<TableRowData>
{
	private LocalDate			date;
	private long				duration;
	private String				description;

	private static final String	tableRowTemplate	= "<tr>\n"
															+ "<td>%s</td>\n"
															+ "<td>%s</td>\n"
															+ "<td>%s</td>\n"
															+ "</tr>";

	public TableRowData(String dateString, String durationString, String description)
	{
		setDate(dateString);
		setDuration(durationString);
		this.description = description;
	}

	public TableRowData(LocalDate date, long durations, String description)
	{
		this.date = date;
		duration = durations;
		this.description = description;
	}

	public LocalDate getDate()
	{
		return date;
	}

	public void setDate(String dateString)
	{
		date = LocalDate.parse(dateString);
	}

	public void setDate(LocalDate date)
	{
		this.date = date;
	}

	public long getDuration()
	{
		return duration;
	}

	public void setDuration(String durationString)
	{
		String[] durString = durationString.split(":");
		duration = Duration.of(Long.parseLong(durString[0]), ChronoUnit.HOURS).getSeconds()
				+ Duration.of(Long.parseLong(durString[1]), ChronoUnit.MINUTES).getSeconds();
	}

	public void setDuration(long duration)
	{
		this.duration = duration;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		return String.format(tableRowTemplate, Main.dateFormatter.format(date), getDurationAsString(), description);
	}

	public String getDurationAsString()
	{
		return String.format("%02d:%02d", duration / 3600, (duration % 3600) / 60);
	}

	@Override
	protected TableRowData clone()
	{
		return new TableRowData(LocalDate.from(date), duration, description);
	}

	@Override
	public int compareTo(TableRowData o)
	{
		return date.compareTo(o.date);
	}
}
