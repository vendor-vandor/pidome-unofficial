/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.dialogs;

/**
 *
 * @author John Sirach
 */
public class ErrorMessage {
    
    public static void display(String title, String message){
        BaseDialog dialog = new BaseDialog(BaseDialog.ERRORMESSAGE);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }
    
}
