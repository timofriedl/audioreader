package com.timofriedl.audioreader;

import javax.sound.sampled.AudioFormat;

/**
 * Represents a series of audio frames that consist of one or multiple channels.
 * 
 * @author Timo Friedl
 */
public class AudioData {

	/**
	 * the normalized audio values for each frame and channel
	 */
	private final double[][] data;

	/**
	 * the format of the audio data
	 */
	private final AudioFormat audioFormat;

	/**
	 * Creates a new audio data instance.
	 */
	public AudioData(double[][] data, AudioFormat audioFormat) {
		this.data = data;
		this.audioFormat = audioFormat;
	}

	/**
	 * Extracts the two dimensional audio data to the one dimensional sample array
	 * for one channel.
	 * 
	 * @param channelNumber the number of the channel to extract
	 * @return the normalized audio values for this channel
	 */
	public double[] extractChannel(int channelNumber) {
		if (channelNumber < 0 || channelNumber >= data[0].length)
			throw new IllegalArgumentException("Channel " + channelNumber + " does not exist.");

		final double[] samples = new double[data.length];

		for (int s = 0; s < samples.length; s++)
			samples[s] = data[s][channelNumber];

		return samples;
	}

	/**
	 * Prints some helpful details about the given {@link AudioFormat}.
	 */
	public void printFormatDetails() {
		if (audioFormat == null) {
			System.out.println("No existing audio format.");
			return;
		}

		System.out.println("Channels:\t" + audioFormat.getChannels());
		System.out.println("Frame rate:\t" + audioFormat.getFrameRate() + " Hz");
		System.out.println("Frame size:\t" + audioFormat.getFrameSize() + " Bytes");
		System.out.println("Sample rate:\t" + audioFormat.getSampleRate() + " Hz");
		System.out.println("Sample size:\t" + audioFormat.getSampleSizeInBits() + " bits");
		System.out.println("Encoding:\t" + audioFormat.getEncoding());
		System.out.println("Big Endian:\t" + audioFormat.isBigEndian());
	}

	/**
	 * @return the normalized audio values for each frame and channel
	 */
	public double[][] getData() {
		return data;
	}

	/**
	 * @return the format of the audio data
	 */
	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

}
