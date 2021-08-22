use std::io::{Read, Write};

fn main() {
    println!("Enumerating ports...");
    let ports = serialport::available_ports().expect("No ports found!");
    for (i, port) in ports.iter().enumerate() {
        println!("{}. {}", i, port.port_name);
    }

    // Let user select port
    print!("Select port: ");
    std::io::stdout().flush().unwrap();

    let mut buffer = String::new();
    let stdin = std::io::stdin();
    stdin.read_line(&mut buffer).unwrap();
    let number: usize = buffer.trim_end().parse().unwrap();
    let port_info = &ports[number];

    println!("Selected {}", port_info.port_name);

    // Perform test operations
    let baudrate = 115200;
    let mut port = serialport::new(&port_info.port_name, baudrate)
        .open()
        .expect("Failed to open port");

    // Write to memory
    port.write_all(&[0, 0, 42]).unwrap();
    port.write_all(&[1, 0, 26]).unwrap();

    // Read from memory
    let mut serial_buf: Vec<u8> = vec![0; 1];

    port.write_all(&[0, 1]).unwrap();
    port.read_exact(&mut serial_buf).unwrap();
    println!("Data: {:?}", serial_buf);

    port.write_all(&[1, 1]).unwrap();
    port.read_exact(&mut serial_buf).unwrap();
    println!("Data: {:?}", serial_buf);
}
