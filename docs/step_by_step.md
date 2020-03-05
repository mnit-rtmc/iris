# Step-by-step Guides

## Create DMS, Comm Link, and Controller

 1. Make sure [edit mode] is **ON**
 2. Create a new **DMS**
    - Select the `View ➔ Message Signs ➔ DMS` menu item to display the DMS form
    - Type the name of the new DMS in the text field at the bottom of the form
      and press the `Create` button
    - Find the new DMS in the table and select it
    - Click the `Properties` button and a DMS properties form will appear
    - On the Location Tab specify the road and location of the sign
    - Close the DMS properties form
 3. Create a new **Comm Link**
    - Select the `View ➔ Maintenance ➔ Comm Links` menu item to display the
      Comm Links form
    - Type the name of the new comm link in the text field in the middle of the
      form and press the `Create` button
    - Find the newly created comm link in the table in the top of the form and
      select it
    - Enter a more complete description in the Description field as needed
    - Enter the controller URI in the URI column, e.g.
      `tcp://10.20.30.40:1234` or `udp://10.20.30.40:1234` for signs that use
      UDP
    - Select the desired protocol
    - Enter the desired polling period and timeout
    - Enable the Comm Link by clicking the `Enabled` checkbox
 4. Create a new **Controller** associated with the comm link
    - In the Comm Link Form with the desired comm link selected, click the
      `Create` button on the bottom of the form
    - Select the new controller in the lower table and click the `Properties`
      button.  The Controller form should appear
    - On the Controller Form Setup Tab, change the condition to active
    - On the Cabinet Tab specify the DMS cabinet location and style
    - On the Controller Form IO Tab, for Pin=1, select DMS in the `Type` column
    - In the Device column, select the name of the DMS created above
 5. View DMS Status
    - If the comm link is enabled (per above) and the controller condition is
      active (per above), the number of successful and failed operations is
      visible in real-time on the Controller's Status Tab
    - View the DMS configuration on the DMS Form Configuration tab.  The Query
      Configuration button on that tab manually queries the sign
    - The Status Tab on the DMS Form shows additional status information

## Create Plan to Activate DMS Messages

 1. Make sure [edit mode] is **ON**.
 2. Select the `View ➔ Plans and Schedules` menu item to display the **Plans and
    Schedules** form.
 3. Create a Phase (if it doesn't exist already)
    - Click the `Plan Phases` tab
    - In the space next to the `Create` button, enter the name of the new phase,
      e.g. `deployed`
    - Press the `Create` button
    - Repeat to add an `undeployed` phase
 4. Create an Action Plan
    - Click the `Action Plans` tab
    - Select `undeployed` for the `Default Phase`
    - Type the name of the new Action Plan in the edit field, e.g. `slick_roads`
    - Press the `Create` button
    - The new Plan should appear in the lower table
 5. Create DMS Actions
    - Click on the Action Plan just created (e.g. `slick_roads`)
    - Click the DMS Actions tab
    - In the edit field on the bottom of the form, type the name of an existing
      sign group
    - Press the `Create` button
    - The new DMS Action should appear in the table
    - Select the `deployed` phase for the DMS action
    - Click on the `Quick Message` column in the DMS Action just created
    - Specify the name of an existing [quick message] to send to the sign
    - Repeat the above DMS Actions steps as many times as desired to send a
      message to additional sign groups
 6. Activate the messages on the signs
    - Select the `Plan` tab to the left of the map
    - Locate the action plan from the list and select it
    - In the `Selected Action Plan` area, change the phase to `deployed`
    - Signs are activated and deactivated every 30 seconds (at `:29` and `:59`),
      so wait at least 30 seconds for the messages to appear or disappear
    - Change the phase back to `undeployed` to blank the signs

Notes:
 - Before running through these steps, create the Sign Groups and Quick Messages
   desired
 - The quick messages that the Action Plan references don't have to be assigned
   to a Sign Group
 - Use the device debug log to diagnose problems with scheduled messages not
   appearing on signs


[edit mode]: user_interface.html#edit-mode
[quick message]: dms.html#quick-messages
