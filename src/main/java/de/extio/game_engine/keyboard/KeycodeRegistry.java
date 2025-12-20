/* Copyright (C) 2023 Stephan Birkl - All Rights Reserved.
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.extio.game_engine.keyboard;

import java.util.Collection;
import java.util.List;

/**
 * Key code registry let's modules register key codes and returns the actual keys configured by the user  
 */
public interface KeycodeRegistry {
	
	/**
	 * Updates the key configured for key registrations - usually used by options menu for the user to change keys
	 */
	void register(KeycodeRegistration... keycodeRegistrations);
	
	/**
	 * Registers key code registrations also containing the default key - usually used by modules making use of these key presses 
	 */
	void registerDefault(KeycodeRegistration... keycodeRegistrations);
	
	/**
	 * Unregisters key code registrations
	 */
	void unregister(String... qualifiers);
	
	/**
	 * Returns the current registered key code registration by qualifier
	 */
	KeycodeRegistration get(String qualifier);
	
	/**
	 * Returns the current registered key code registration by code
	 */
	List<KeycodeRegistration> get(int code);
	
	/**
	 * Returns all key code registrations
	 */
	Collection<KeycodeRegistration> getAll();
	
	/**
	 * Checks whether the key code and modifiers (ctrl, alt, ...) match to a certain registration. This method is used by modules in key event handlers to check whether their key was pressed
	 */
	boolean check(String qualifier, int code, int modifiers);
	
}
