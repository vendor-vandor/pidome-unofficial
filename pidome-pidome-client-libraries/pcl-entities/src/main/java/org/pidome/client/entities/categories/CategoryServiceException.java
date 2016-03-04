/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.categories;

/**
 *
 * @author John
 */
public class CategoryServiceException extends Exception {

    /**
     * Creates a new instance of <code>CategoryServiceException</code> without
     * detail message.
     */
    public CategoryServiceException() {
    }

    /**
     * Constructs an instance of <code>CategoryServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CategoryServiceException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>CategoryServiceException</code> with the
     * specified throwable.
     *
     * @param ex the throwable.
     */
    public CategoryServiceException(Throwable ex) {
        super(ex);
    }
}
