package de.upb.manuel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main extends JFrame
{
	/**
	 * 
	 */
	private static final long				serialVersionUID	= 3080797911952478607L;

	private static final String				tableRowTemplate	= "<tr>\n"
																		+ "<td>%s</td>\n"
																		+ "<td>%s</td>\n"
																		+ "<td>%s</td>\n"
																		+ "</tr>";
	private static final DateTimeFormatter	dateFormatter		= DateTimeFormatter.ISO_DATE;
	boolean									taskRunning			= false;
	TemporalField							dayOfWeek			= WeekFields.of(Locale.GERMANY).dayOfWeek();

	public static void main(String[] args) throws IOException
	{
		new Main();
	}

	public Main() throws IOException
	{
		String pathToTimeSheetsFolder = null;
		Properties props = loadProperties();
		if (props.containsKey("pathToTimeSheets"))
		{
			pathToTimeSheetsFolder = props.getProperty("pathToTimeSheets");
		}
		else
		{
			JFileChooser chooseTimeSheetsPath = new JFileChooser(".");
			chooseTimeSheetsPath.setDialogTitle("Please select your time sheet Folder");
			chooseTimeSheetsPath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			while (pathToTimeSheetsFolder == null)
			{
				if (chooseTimeSheetsPath.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				{
					pathToTimeSheetsFolder = chooseTimeSheetsPath.getSelectedFile().getPath();
					if (JOptionPane.showConfirmDialog(this, "Set " + pathToTimeSheetsFolder + " as your folder?") != JOptionPane.YES_OPTION)
					{
						pathToTimeSheetsFolder = null;
					}
				}
				else
				{
					System.exit(0);
				}
			}
			props.put("pathToTimeSheets", pathToTimeSheetsFolder);
			saveProps(props);
		}

		setLayout(new BorderLayout());
		LocalDate today = LocalDate.now();
		Locale locale = Locale.GERMANY;
		int weekOfYear = today.get(WeekFields.of(locale).weekOfWeekBasedYear());
		String fileNameForThisWeek = String.format("calendar-week-%02d.md", weekOfYear);
		Path timeSheet = Paths.get(pathToTimeSheetsFolder, fileNameForThisWeek);
		List<String> header = new ArrayList<>();
		List<String> table = new ArrayList<>();
		String name = null;
		if (props.containsKey("name"))
		{
			name = props.getProperty("name");
		}
		while (name == null)
		{
			name = JOptionPane.showInputDialog("Please type your name");
		}
		props.setProperty("name", name);

		String title = String.format("TimeTrackr - %s - CW %02d", name, weekOfYear);
		setTitle(title);
		if (Files.exists(timeSheet))
		{
			List<String> contents = Files.readAllLines(timeSheet);
			String line;
			int i = 0;
			while (!(line = contents.get(i++)).equals("<table>"))
			{
				header.add(line);
			}
			if (!props.containsKey("name"))
			{
				for (String headerline : header)
				{
					if (headerline.startsWith("##"))
					{
						name = headerline.substring(headerline.indexOf("(") + 1, headerline.indexOf(")")).trim();
						props.setProperty("name", name);
					}
				}
			}
			saveProps(props);
			table.add(line);
			while (!(line = contents.get(i++)).equals("</table>"))
			{
				table.add(line);
			}
			table.add(line);
			System.out.println("Header:");
			System.out.println(header.stream().collect(Collectors.joining("\n")));

			System.out.println("Table:");
			System.out.println(table.stream().collect(Collectors.joining("\n")));
		}
		else
		{

			header.add("# Time sheet");
			header.add("");
			header.add(String.format("## Calendar week: %d (%s)", weekOfYear, name));
			header.add("");
			header.add(String.format("Dates: %s - %s", dateFormatter.format(LocalDate.now().with(dayOfWeek, 1)),
					dateFormatter.format(LocalDate.now().with(dayOfWeek, 7))));
			header.add("");

			table.add("<table>");
			table.add("<tr>");
			table.add("<td><strong>Date</strong></td>");
			table.add("<td><strong>Hours</strong></td>");
			table.add("<td><strong>Description</strong></td>");
			table.add("</tr>");
			table.add("</table>");
		}

		JButton startStopButton = new JButton("Start Activity");
		add(startStopButton, BorderLayout.CENTER);
		startStopButton.addActionListener(new ActionListener()
		{
			ScheduledExecutorService	ses;
			long						startTimeStamp	= 0;
			String						description		= "";

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!taskRunning)
				{
					taskRunning = true;
					while (description.trim().isEmpty())
					{
						description = JOptionPane.showInputDialog("Describe what you're doing");
					}
					setTitle(description);
					startTimeStamp = System.currentTimeMillis();
					ses = Executors.newSingleThreadScheduledExecutor();
					ses.scheduleAtFixedRate(() -> {
						long seconds = (System.currentTimeMillis() - startTimeStamp) / 1000;
						startStopButton
								.setText(String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60) + "   STOP");
					} , 0, 1, TimeUnit.SECONDS);
				}
				else
				{
					taskRunning = false;
					try
					{
						ses.shutdown();
						ses.awaitTermination(2, TimeUnit.SECONDS);
					}
					catch (InterruptedException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					long seconds = (System.currentTimeMillis() - startTimeStamp) / 1000;
					String durationString = String.format("%02d:%02d", seconds / 3600, (seconds % 3600) / 60);

					String newRow = String.format(tableRowTemplate, dateFormatter.format(LocalDate.now()), durationString, description);
					table.add(table.size() - 1, newRow);
					startStopButton.setText("Start Activity");
					saveToFile(header, table, timeSheet);
					description = "";
					setTitle(title);
				}
			}

		});
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(500, 500);
		setVisible(true);
	}

	private void saveProps(Properties props) throws IOException
	{
		props.store(Files.newOutputStream(Paths.get("timetrackr.properties")), "Properties of TimeTrackr");
	}

	private Properties loadProperties() throws IOException
	{
		Properties props = new Properties();
		if (Files.exists(Paths.get("timetrackr.properties")))
		{
			props.load(Files.newInputStream(Paths.get("timetrackr.properties")));
		}
		return props;
	}

	private void saveToFile(List<String> header, List<String> table, Path timeSheet)
	{
		List<String> lines = new ArrayList<>();
		lines.addAll(header);
		lines.addAll(table);
		try
		{
			Files.write(timeSheet, lines);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
