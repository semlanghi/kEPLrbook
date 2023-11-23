import csv
import matplotlib.pyplot as plt

import csv
import matplotlib.pyplot as plt

def plot_csv(csv_file):
    # Lists to store values from the CSV columns
    x_values_1 = []
    y_values_1 = []
    x_values_2 = []
    y_values_2 = []

    # Read the CSV file and extract values
    with open(csv_file, 'r') as file:
        csv_reader = csv.reader(file)
        next(csv_reader)  # Skip the header row if it exists
        for row in csv_reader:
            # Assuming the 5th column is at index 4, the 6th column is at index 5,
            # the 7th column is at index 6, and the 8th column is at index 7
            x_values_1.append(float(row[6]))
            y_values_1.append(float(row[4]))
            x_values_2.append(float(row[6]))
            y_values_2.append(float(row[7]))

    # Create a figure with two subplots in a single column
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 4))

    # Plot for the first set of columns
    ax1.plot(x_values_1, y_values_1, label='Sine Curve', color='blue')
    ax1.scatter(x_values_1, y_values_1, marker='o', color='b',label='Highlighted Points')
    ax1.set_xlabel('Number of Events', fontsize=20)
    ax1.set_ylabel('Latency (nanoseconds)',fontsize=20)
    ax1.tick_params(axis='both', which='major', labelsize=16)


    # Plot for the second set of columns
    ax2.bar(x_values_2, y_values_2, color='r')
    ax2.set_xlabel('Number of Events', fontsize=20)
    ax2.set_ylabel('Memory (bytes)', fontsize=20)
    ax2.tick_params(axis='both', which='major', labelsize=16)


    # Adjust layout to prevent clipping of labels
    plt.tight_layout()

    # Show the plot
    plt.savefig('everyeveryperf.pdf', dpi=300)
    plt.show()

# Replace 'your_file.csv' with the actual path to your CSV file
plot_csv('performance-no-script-latency.csv')