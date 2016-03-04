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
public class CategoryNotAvailableException extends Exception {

    /**
     * Creates a new instance of <code>CategoryNotAvailableException</code>
     * without detail message.
     */
    public CategoryNotAvailableException() {
    }

    /**
     * Constructs an instance of <code>CategoryNotAvailableException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public CategoryNotAvailableException(String msg) {
        super(msg);
    }
}
