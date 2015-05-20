/*******************************************************************************
 * Copyright (c) 2012 - 2015 Signal Iduna Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Signal Iduna Corporation - initial API and implementation
 * akquinet AG
 *******************************************************************************/
/**
 * 
 */
package org.testeditor.fixture.swing;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.fest.swing.core.BasicRobot;
import org.fest.swing.core.ComponentFinder;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.Robot;
import org.fest.swing.driver.BasicJTableCellReader;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JCheckBoxFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JRadioButtonFixture;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.launcher.ApplicationLauncher;
import org.testeditor.fixture.core.elementlist.ElementListService;
import org.testeditor.fixture.core.exceptions.ElementKeyNotFoundException;
import org.testeditor.fixture.core.interaction.Fixture;
import org.testeditor.fixture.core.utils.ExceptionUtils;

/**
 * Fixture for communication via socket with swing agent.
 * 
 */
public class SwingFixture implements Fixture {
	private static final Logger LOGGER = Logger.getLogger(SwingFixture.class);
	private static Thread thread;
	private Robot robot;
	private FrameFixture window;

	private ElementListService elementListService;

	/**
	 * Creates the element list instance representing the GUI-Map for widget
	 * element id's of an application and the user defined names for this
	 * represented GUI element. Often used in a FitNesse ScenarioLibrary for
	 * configuration purpose. <br />
	 * 
	 * Usage for FitNesse: |set elementlist|../ElementList/content.txt|
	 * 
	 * @param elementList
	 *            relative path of the element list content.txt wiki site on a
	 *            FitNesse Server where WikiPages is the directory where all the
	 *            Wiki Sites of the recent project are
	 */
	public void setElementlist(String elementList) {
		this.elementListService = ElementListService.instanceFor(elementList);
	}

	/**
	 * Returns the locator for a given key.
	 * 
	 * @param elementListKey
	 *            key in the ElementList
	 * @return locator as String
	 */
	protected String getLocatorFromElementList(String elementListKey) {
		String locator = null;

		try {
			locator = elementListService.getValue(elementListKey);
		} catch (ElementKeyNotFoundException e) {
			ExceptionUtils.handleElementKeyNotFoundException(elementListKey, e);
		}

		return locator;
	}

	/**
	 * 
	 * @param path
	 */

	public boolean startApplication(final String path) {
		startApplicationThread(path, null, Thread.currentThread().getContextClassLoader());
		return true;
	}

	/**
	 * 
	 * @param path
	 * @param args2
	 * @param cl
	 */
	public void startApplicationThread(final String path, final String[] args2, ClassLoader cl) {
		thread = new Thread() {

			@Override
			public void run() {
				ApplicationLauncher.application(path).start();
			}
		};
		cl = Thread.currentThread().getContextClassLoader();
		thread.setContextClassLoader(cl);
		thread.start();

		robot = BasicRobot.robotWithCurrentAwtHierarchy();
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return frame.isActive() && frame.isFocused();
			}
		}).using(robot);
	}

	/**
	 * Stops running AUT.
	 * 
	 */
	public boolean stopApplication() {
		robot.cleanUp();
		return true;
	}

	/**
	 * Search and return the Component with the element list key.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @return Found component
	 */
	protected Component findComponent(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		Component result = null;
		try {
			ComponentFinder finder = window.robot.finder();
			Component component = finder.findByName(locator);
			if (component != null) {
				result = component;
			} else {
				LOGGER.error("The Component with the name " + locator + " could not be found.");
			}
		} catch (Exception e) {
			LOGGER.error("find Component Error: " + e);
		}
		return result;
	}

	/**
	 * Insert the Text into a JTextField.
	 * 
	 * @param text
	 *            The Text to fill the TextField
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean insertIntoTextField(String elementListKey, String text) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JTextComponentFixture textField = window.textBox(locator);
			textField.enterText(text);
			return true;
		} catch (Exception e) {
			LOGGER.error("insert text into a textField Error: " + e);
			return false;
		}
	}

	/**
	 * delete the text form the textField.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean deleteTextField(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JTextComponentFixture textField = window.textBox(locator);
			textField.deleteText();
			return true;
		} catch (Exception e) {
			LOGGER.error("delete text from textField Error: " + e);
			return false;
		}
	}

	/**
	 * Get the Text from the JTextField.
	 * 
	 * @param elementListKey
	 * @return
	 */
	public String getTextFromTextField(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		String result = null;
		try {
			JTextComponentFixture textField = window.textBox(locator);
			result = textField.text();
		} catch (Exception e) {
			LOGGER.error("get text from textField Error: " + e);
		}
		return result;
	}

	/**
	 * Click the button with the name.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean clickButton(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JButtonFixture button = window.button(locator);
			button.click();
			return true;
		} catch (Exception e) {
			LOGGER.error("click Button Error: " + e);
			return false;
		}
	}

	/**
	 * select the item by the text of the comboBox item.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param item
	 *            name of the item
	 */
	public boolean selectComboBoxItemByName(String elementListKey, String item) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JComboBoxFixture comboBoxFixture = window.comboBox(locator);
			comboBoxFixture.selectItem(item);
			return true;
		} catch (ComponentLookupException e) {
			LOGGER.error("No or more then one comboBox found Error: " + e);
			return false;
		} catch (Exception e) {
			LOGGER.error("could not select the item in comboBox Error: " + e);
			return false;
		}
	}

	/**
	 * select the item by the index of the comboBox item.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param index
	 *            index of the item
	 */
	public boolean selectComboBoxItemById(String elementListKey, int index) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JComboBoxFixture comboBoxFixture = window.comboBox(locator);
			comboBoxFixture.selectItem(index);
			return true;
		} catch (ComponentLookupException e) {
			LOGGER.error("No or more then one comboBox found Error: " + e);
			return false;
		} catch (Exception e) {
			LOGGER.error("could not select the item in comboBox Error: " + e);
			return false;
		}
	}

	/**
	 * clears the selection from the ComboBox.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean clearSelectionComboBox(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JComboBoxFixture comboBox = window.comboBox(locator);
			comboBox.clearSelection();
			return true;
		} catch (ComponentLookupException e) {
			LOGGER.error("No or more then one comboBox found Error: " + e);
			return false;
		} catch (Exception e) {
			LOGGER.error("could not clear the selection in comboBox Error: " + e);
			return false;
		}
	}

	/**
	 * Returns the text of the selected item in the ComboBox.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @return text of the selected Item
	 */
	public String getSelectedComboBoxItemText(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		String result = null;
		try {
			JComboBoxFixture comboBox = window.comboBox(locator);
			result = comboBox.valueAt(comboBox.target.getSelectedIndex());
		} catch (ComponentLookupException e) {
			LOGGER.error("No or more then one comboBox found Error: " + e);
		} catch (Exception e) {
			LOGGER.error("could get the text of the selected item Error: " + e);
		}
		return result;
	}

	/**
	 * Returns the id of the selected item in the ComboBox.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @return id of the selected Item
	 */
	public int getSelectedComboBoxItemId(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		int result = -2;
		try {
			JComboBoxFixture comboBox = window.comboBox(locator);
			result = comboBox.target.getSelectedIndex();
		} catch (ComponentLookupException e) {
			LOGGER.error("No or more then one comboBox found Error: " + e);
		} catch (Exception e) {
			LOGGER.error("could get the id of the selected item Error: " + e);
		}
		return result;
	}

	/**
	 * Check the radioButton.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean checkRadioButton(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		LOGGER.debug("elementListKey: " + elementListKey);
		LOGGER.debug("locator: " + locator);
		try {
			JRadioButtonFixture radioButton = window.radioButton(locator);
			radioButton.check();
			return true;
		} catch (Exception e) {
			LOGGER.error("could not check the radioButton Error: " + e);
			return false;
		}
	}

	/**
	 * Uncheck Radiobutton
	 * 
	 * @param elementListKey
	 */
	public boolean uncheckRadioButton(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		LOGGER.debug("elementListKey: " + elementListKey);
		LOGGER.debug("locator: " + locator);
		try {
			JRadioButtonFixture radioButton = window.radioButton(locator);
			radioButton.uncheck();
			return true;
		} catch (Exception e) {
			LOGGER.error("could not check the radioButton Error: " + e);
			return false;
		}
	}

	/**
	 * Check the radioButton.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @return boolean State of the radioButton
	 */
	public boolean isCheckedRadioButton(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		boolean result = false;
		try {
			JRadioButtonFixture radioButton = window.radioButton(locator);
			result = radioButton.target.isSelected();
		} catch (Exception e) {
			LOGGER.error("could not get the State of the radioButton Error: " + e);
		}
		return result;
	}

	/**
	 * check the checkBox.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean checkCheckBox(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JCheckBoxFixture checkBox = window.checkBox(locator);
			checkBox.check();
			return true;
		} catch (Exception e) {
			LOGGER.error("could not check the checkBox Error: " + e);
			return false;
		}
	}

	/**
	 * uncheck the checkBox.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean uncheckCheckBox(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JCheckBoxFixture checkBox = window.checkBox(locator);
			checkBox.uncheck();
			return true;
		} catch (Exception e) {
			LOGGER.error("could not uncheck the checkBox Error: " + e);
			return false;
		}
	}

	/**
	 * returns the state of the checkBox.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @return boolean state of the checkBox
	 */
	public boolean isCheckedCheckBox(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		boolean result = false;
		try {
			JCheckBoxFixture checkBox = window.checkBox(locator);
			result = checkBox.target.isSelected();
		} catch (Exception e) {
			LOGGER.error("could get the State of the checkBox Error: " + e);
		}
		return result;
	}

	/**
	 * select the Row with the index from the table.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param Id
	 *            index from the Row
	 */
	public boolean selectTableRowById(String elementListKey, int Id) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JTableFixture table = window.table(locator);
			if (Id <= table.rowCount()) {
				table.selectRows(Id);
				return true;
			} else {
				LOGGER.error("Id was not in range from the tabel row count.");
				return false;
			}

		} catch (Exception e) {
			LOGGER.error("could not select the Row from the tabel Error: " + e);
			return false;
		}
	}

	/**
	 * Compares table entry with given value
	 * 
	 * @param elementListKey
	 * @param value
	 * @param column
	 * @return if true or false
	 */
	public boolean checkTableCellValue(String elementListKey, String value, String column) {
		String locator = getLocatorFromElementList(elementListKey);
		int colLocator = Integer.parseInt(getLocatorFromElementList(column));
		String content = null;
		try {
			JTableFixture table = window.table(locator);

			BasicJTableCellReader cellReader = new BasicJTableCellReader();
			content = cellReader.valueAt(table.target, (table.rowCount() - 1), colLocator);

		} catch (Exception e) {
			LOGGER.error("could not select the Row from the tabel Error: " + e);
			return false;
		}
		return (value.equals(content));
	}

	/**
	 * Double click the row of the table.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param Id
	 *            index from the Row
	 */
	public boolean doubleClickTableRowById(String elementListKey, int Id) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JTableFixture table = window.table(locator);
			selectTableRowById(locator, Id);
			table.doubleClick();
			return true;
		} catch (Exception e) {
			LOGGER.error("could not double click the tabel Error: " + e);
			return false;
		}
	}

	/**
	 * returns the index from the selected row from the table.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @return int Index of the selection
	 */
	public int getSelectedTableRowIndex(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		int result = -2;
		try {
			JTableFixture table = window.table(locator);
			result = table.target.getSelectedRow();
		} catch (Exception e) {
			LOGGER.error("could not get the selected index the tabel Error: " + e);
		}
		return result;
	}

	/**
	 * double click the component with the name.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 */
	public boolean doubleClickComponent(String elementListKey) {
		String locator = getLocatorFromElementList(elementListKey);
		try {
			JComponent component = (JComponent) findComponent(locator);
			window.robot.doubleClick(component);
			return true;
		} catch (Exception e) {
			LOGGER.error("could not doubleClick the component Error: " + e);
			return false;
		}
	}

	/**
	 * wait milli seconds
	 * 
	 * @param milliSeconds
	 *            milliseconds to wait
	 */
	public boolean waitMilliSeconds(int milliSeconds) {
		try {
			if (milliSeconds > 0) {
				window.wait(milliSeconds);

			}
			return true;
		} catch (Exception e) {
			LOGGER.error("could not wait/timeout: " + e);
			return false;
		}
	}

	/**
	 * returns true when element is enabled, false otherwise.
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @return
	 */
	public boolean isElementEnabled(String elementListKey) {
		boolean result = false;
		try {
			JComponent component = (JComponent) findComponent(elementListKey);
			result = component.isEnabled();
		} catch (Exception e) {
			LOGGER.error("could not find out state, Error: " + e);
		}
		return result;
	}

	/**
	 * returns true when the compared texts are identical
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param text
	 *            text to compare
	 * @return boolean Status of consent
	 */
	public boolean checkIfTextEquals(String elementListKey, String text) {
		return (text.equals(getTextFromTextField(elementListKey)));
	}

	/**
	 * returns true when the compared texts are not identical
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param text
	 *            text to compare
	 * @return boolean Status of non-compliance
	 */
	public boolean checkIfTextNotEquals(String elementListKey, String text) {
		return !(text.equals(getTextFromTextField(elementListKey)));
	}

	/**
	 * returns true when the compared texts are identical
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param text
	 *            text to compare
	 * @return boolean Status of consent
	 */
	public boolean checkIfSelectedItemIs(String elementListKey, String text) {
		LOGGER.debug("ComboBox: " + getSelectedComboBoxItemText(elementListKey));
		LOGGER.debug("Vorgegebener Text: " + text);
		return (text.equals(getSelectedComboBoxItemText(elementListKey)));
	}

	/**
	 * returns true when the compared texts are not identical
	 * 
	 * @param elementListKey
	 *            Key of the Component in element list
	 * @param text
	 *            text to compare
	 * @return boolean Status of non-compliance
	 */
	public boolean checkIfSelectedItemIsNot(String elementListKey, String text) {
		return !(text.equals(getSelectedComboBoxItemText(elementListKey)));
	}

	@Override
	public String getTestName() {
		return null;
	}

	@Override
	public void postInvoke(Method arg0, Object arg1, Object... arg2) throws InvocationTargetException,
			IllegalAccessException {
	}

	@Override
	public void preInvoke(Method arg0, Object arg1, Object... arg2) throws InvocationTargetException,
			IllegalAccessException {
	}

	@Override
	public void setTestName(String arg0) {

	}
}
