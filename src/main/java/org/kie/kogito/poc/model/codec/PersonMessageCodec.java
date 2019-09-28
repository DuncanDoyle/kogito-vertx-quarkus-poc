package org.kie.kogito.poc.model.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.kie.kogito.poc.model.Person;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * PersonMessageCodec
 */
public class PersonMessageCodec implements MessageCodec<Person, Person> {

    @Override
    public Person decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        // Length is defined in the first 4 bytes, so move the position.
        pos += 4;
        byte[] bytes = buffer.getBytes(pos, pos + length);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Person person = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            person = (Person) ois.readObject();
        } catch (IOException ioe) {
            throw new RuntimeException("Error while reading Command.", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Error reading Command.", cnfe);
        }
        return person;
    }

    @Override
    public void encodeToWire(Buffer buffer, Person person) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(person);
        } catch (IOException ioe) {
            throw new RuntimeException("Error while writing Person.", ioe);
        }

        byte[] flightInfoBytes = baos.toByteArray();
        buffer.appendInt(flightInfoBytes.length);
        buffer.appendBytes(flightInfoBytes);

    }

    @Override
    public String name() {
        //return this.getClass().getSimpleName();
        return "person";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

    @Override
    public Person transform(Person person) {
        return person;
    }

}