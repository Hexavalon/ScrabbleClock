package com.gmail.hexragonat.clockGadget;

import javafx.application.Platform;

import java.util.Calendar;
import java.util.concurrent.*;

public class ClockHeartbeat implements Runnable
{
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	private final ClockController controller;

	private ScheduledFuture<?> task;

	ClockHeartbeat(ClockController controller)
	{
		this.controller = controller;
	}

	public void start()
	{
		task = exec.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
	}

	public void stop()
	{
		if (!task.isCancelled()) task.cancel(false);
	}

	@Override
	public void run()
	{
		@SuppressWarnings("unchecked")
		FutureTask updateUITask = new FutureTask(() ->
		{
			//controller.clearAll();
			Calendar now = Calendar.getInstance();
			//now.set(Calendar.MINUTE, new Random().nextInt(60));
			//now.set(Calendar.HOUR, 12);
			//now.set(Calendar.MINUTE, 25);
			//now.set(Calendar.AM_PM, Calendar.PM);
			consume(now);
		}, null);

		Platform.runLater(updateUITask);
	}

	private void consume(Calendar now)
	{
		WordEnum hourEnum = null;
		WordEnum progressEnum;
		WordEnum minuteEnum;

		int hour = now.get(Calendar.HOUR);
		boolean isAM = now.get(Calendar.AM_PM) == Calendar.AM;

		int minute = now.get(Calendar.MINUTE);

		int displayedHour = hour;

		if (minute < 5)
		{
			minuteEnum = null;
			progressEnum = WordEnum.P_OCLOCK;
		}
		else if (minute < 10)
		{
			minuteEnum = WordEnum.M_FIVE;
			progressEnum = WordEnum.P_PAST;
		}
		else if (minute < 15)
		{
			minuteEnum = WordEnum.M_TEN;
			progressEnum = WordEnum.P_PAST;
		}
		else if (minute < 20)
		{
			minuteEnum = WordEnum.M_QUARTER;
			progressEnum = WordEnum.P_PAST;
		}
		else if (minute < 25)
		{
			minuteEnum = WordEnum.M_TWENTY;
			progressEnum = WordEnum.P_PAST;
		}
		else if (minute < 30)
		{
			minuteEnum = WordEnum.M_TWENTYFIVE;
			progressEnum = WordEnum.P_PAST;
		}
		else if (minute < 35)
		{
			minuteEnum = WordEnum.M_HALF;
			progressEnum = WordEnum.P_PAST;
		}
		else if (minute < 40) //
		{
			minuteEnum = WordEnum.M_TWENTYFIVE;
			progressEnum = WordEnum.P_TIL;
		}
		else if (minute < 45)
		{
			minuteEnum = WordEnum.M_TWENTY;
			progressEnum = WordEnum.P_TIL;
		}
		else if (minute < 50)
		{
			minuteEnum = WordEnum.M_QUARTER;
			progressEnum = WordEnum.P_TIL;
		}
		else if (minute < 55)
		{
			minuteEnum = WordEnum.M_TEN;
			progressEnum = WordEnum.P_TIL;
		}
		else
		{
			minuteEnum = WordEnum.M_FIVE;
			progressEnum = WordEnum.P_TIL;
		}

		if (progressEnum == WordEnum.P_TIL) displayedHour++;

		if (displayedHour == 12)
		{
			isAM = !isAM;
		}
		if (displayedHour > 12)
		{
			displayedHour = 1;
		}

		switch (displayedHour)
		{
			case 1:
				hourEnum = WordEnum.H_ONE;
				break;
			case 2:
				hourEnum = WordEnum.H_TWO;
				break;
			case 3:
				hourEnum = WordEnum.H_THREE;
				break;
			case 4:
				hourEnum = WordEnum.H_FOUR;
				break;
			case 5:
				hourEnum = WordEnum.H_FIVE;
				break;
			case 6:
				hourEnum = WordEnum.H_SIX;
				break;
			case 7:
				hourEnum = WordEnum.H_SEVEN;
				break;
			case 8:
				hourEnum = WordEnum.H_EIGHT;
				break;
			case 9:
				hourEnum = WordEnum.H_NINE;
				break;
			case 10:
				hourEnum = WordEnum.H_TEN;
				break;
			case 11:
				hourEnum = WordEnum.H_ELEVEN;
				break;
			case 0:
			case 12:
				if (!isAM) hourEnum = WordEnum.H_NOON;
				else hourEnum = WordEnum.H_MIDNIGHT;
				if (minuteEnum == null) progressEnum = null;
				break;
		}

		//disable all stuff that will be disabled
		for (WordEnum enum0 : WordEnum.values())
		{
			if (!(enum0 == minuteEnum || enum0 == hourEnum || enum0 == progressEnum || enum0 == WordEnum.$_ITS))
			{
				controller.toggle(enum0, false);
			}
		}

		// handle one second delay as delayed transition plays out
		final WordEnum finalMinuteEnum = minuteEnum;
		final WordEnum finalHourEnum = hourEnum;
		final WordEnum finalProgressEnum = progressEnum;
		exec.schedule(() ->
		{
			@SuppressWarnings("unchecked")
			FutureTask toggleOnTask = new FutureTask(() ->
			{
				for (WordEnum enum0 : WordEnum.values())
				{
					if (enum0 == finalMinuteEnum || enum0 == finalHourEnum || enum0 == finalProgressEnum || enum0 == WordEnum.$_ITS)
					{
						if (!enum0.isActive()) controller.toggle(enum0, true);
					}
				}
			}, null);
			Platform.runLater(toggleOnTask);
		}, 1, TimeUnit.SECONDS); //so animations won't overlap and cause weird stuff

		//System.out.println(minuteEnum +"\t\t"+ progressEnum +"\t\t"+ hourEnum);
	}
}