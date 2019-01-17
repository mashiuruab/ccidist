package com.cefalo.cci.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * The base interface to implement binary file storage facilities. The interface does not assume any concrete storage
 * medium. All return values are specific to the storage implementation.
 *
 * @author partha
 *
 */
public interface Storage<T> {
    /**
     * Retrieves an {@link InputStream} to read the binary data of a resource.
     *
     * @param resource
     *            The resource whose data will be read.
     * @return an {@link InputStream} to read the binary content of the resource. The caller must close the input
     *         stream. May return <code>null</code> if the issue does not have a binary file.
     * @throws IOException
     *             if an I/O error occurs.
     *
     */
    InputStream get(final T resource) throws IOException;

    /**
     * Checks if a resource has any binary associated. This is basically a faster alternative for:
     * <code>get(resource) != null</code>
     *
     * @param resource
     *            The resource whose data will be read.
     * @return <code>true</code> if a binary exists for the resource, <code>false</code> otherwise.
     * @throws IOException
     *             if an I/O error occurs.
     *
     */
    boolean exists(final T resource) throws IOException;

    /**
     * Returns an {@link InputStream} for a file inside the resource binary. This method assumes that the resources
     * binary is a compresses Zip/Jar archive.
     *
     * @param resource
     *            the resource. May not be <code>null</code>.
     * @param fragmentPath
     *            the location of the requested file inside the resource binary. May not be <code>null</code>.
     * @return an {@link InputStream} that can be used to read the content of the requested file. The caller must close
     *         the stream. May never return <code>null</code>.
     * @throws IOException
     *             if an I/O error occurs.
     */
    InputStream getFragment(final T resource, final URI fragmentPath) throws IOException;

    /**
     * Stores the binary data in the inputstream for the resource.
     *
     * @param resource
     *            the resource. May not be <code>null</code>
     * @param data
     *            The binary data for the resource. May not be <code>null</code>.
     * @throws IOException
     *             if an I/O error occurs.
     */
    void create(final T resource, final InputStream data) throws IOException;

    /**
     * Updates the resource binary content with the supplied updated data.
     *
     * @param resource
     *            the resource. May not be null.
     * @param updatedData
     *            the updated binary data for the resource.
     * @throws IOException
     *             if an I/O error occurs.
     */
    void update(T resource, final InputStream updatedData) throws IOException;

    /**
     * Deletes the binary data for the storage.
     *
     * @param resource
     *            the resource. May not be <code>null</code>.
     * @throws IOException
     *             if an I/O error occurs.
     */
    void delete(final T resource) throws IOException;

    /**
     * Clean up the binary data from File Storage only but not from the database. This differs from delete method
     * (described above) because during deletion file is deleted from both database and file storage.
     *
     * @param resource
     *            the resource. May not be <code>null</code>.
     *
     */
    void cleanUp(final T resource);
}
