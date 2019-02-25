/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package source.communication_debug;

/**
 *
 * @author mruaro
 */
public class CommDebbugInstance {
    
    private String producer;
    private String consumer;
    private String time;

    public CommDebbugInstance(String producer, String consumer, String time) {
        this.producer = producer;
        this.consumer = consumer;
        this.time = time;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        CommDebbugInstance p = (CommDebbugInstance)obj;
        if (this.producer.equals(p.producer) && this.consumer.equals(p.consumer))
            return true;
        return false;
    }
    
    
    
    
    
}
