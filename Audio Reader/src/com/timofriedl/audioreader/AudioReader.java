package com.timofriedl.audioreader;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * An audio reader that converts *.wav files to an array of <code>double</code>
 * values.
 * 
 * @author Timo Friedl
 */
public abstract class AudioReader {

	/**
	 * Reads the *.wav file at the given {@link #path} and converts the audio data
	 * to an {@link AudioData} instance with formatted <code>double</code> values
	 * for each audio channel in range [-1.0, 1.0]</br>
	 * 
	 * Each sample value is stored as following:</br>
	 * 
	 * <code>double sampleValue = frames[frameNumber][channelNumber]</code>
	 * 
	 * @return the {@link AudioData} instance with the sample data and audio format
	 *         information
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public static AudioData read(String path) throws UnsupportedAudioFileException, IOException {
		final AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
		final AudioFormat audioFormat = ais.getFormat();

		final byte[] bytes = ais.readAllBytes();

		return convertBytesToFrames(bytes, audioFormat);
	}

	/**
	 * Converts raw audio byte values to an {@link AudioData} instance.
	 * 
	 * @param bytes       the raw audio bytes
	 * @param audioFormat the {@link AudioFormat}
	 * @return the formatted audio values
	 * @throws UnsupportedAudioFileException
	 */
	private static AudioData convertBytesToFrames(byte[] bytes, AudioFormat audioFormat)
			throws UnsupportedAudioFileException {
		final int bytesPerFrame = audioFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED ? 1
				: audioFormat.getFrameSize();

		final double[][] frames = new double[bytes.length / bytesPerFrame][];

		for (int f = 0; f < frames.length; f++)
			frames[f] = convertFrameBytesToFrameSamples(
					Arrays.copyOfRange(bytes, f * bytesPerFrame, (f + 1) * bytesPerFrame), audioFormat);

		return new AudioData(frames, audioFormat);
	}

	/**
	 * Converts an array of <code>byte</code>s that represent one frame to its
	 * formatted <code>double</code> array.
	 * 
	 * @param frameBytes  the array of bytes that represent the frame
	 * @param audioFormat the {@link AudioFormat}
	 * @return the formatted frame <code>doubles</code>
	 * @throws UnsupportedAudioFileException if unsupported audio {@link Encoding}
	 */
	private static double[] convertFrameBytesToFrameSamples(byte[] frameBytes, AudioFormat audioFormat)
			throws UnsupportedAudioFileException {
		final double[] samples = new double[audioFormat.getChannels()];

		final int bytesPerSample = audioFormat.getSampleSizeInBits() / Byte.SIZE;

		for (int c = 0; c < samples.length; c++)
			samples[c] = convertSampleBytesToSample(
					Arrays.copyOfRange(frameBytes, c * bytesPerSample, (c + 1) * bytesPerSample), audioFormat);

		return samples;
	}

	/**
	 * Converts an array of <code>byte</code>s that represent one sample to its
	 * formatted <code>double</code> value in range [-1.0, 1.0].
	 * 
	 * @param sampleBytes the array of bytes that represent the sample
	 * @param audioFormat the {@link AudioFormat}
	 * @return the formatted sample value
	 * @throws UnsupportedAudioFileException if unsupported audio {@link Encoding}
	 */
	private static double convertSampleBytesToSample(byte[] sampleBytes, AudioFormat audioFormat)
			throws UnsupportedAudioFileException {
		if (!audioFormat.isBigEndian()) {
			final byte[] rev = new byte[sampleBytes.length];
			for (int b = 0; b < sampleBytes.length; b++)
				rev[b] = sampleBytes[sampleBytes.length - b - 1];
			sampleBytes = rev;
		}

		final int[] unsignedSampleBytes = new int[sampleBytes.length];

		if (audioFormat.getEncoding().equals(Encoding.PCM_SIGNED)) {
			for (int b = 0; b < sampleBytes.length; b++)
				unsignedSampleBytes[b] = sampleBytes[b] + 128;
		} else if (audioFormat.getEncoding().equals(Encoding.PCM_UNSIGNED)) {
			for (int b = 0; b < sampleBytes.length; b++)
				unsignedSampleBytes[b] = sampleBytes[b] & 0xFF;
		} else
			throw new UnsupportedAudioFileException(
					"The encoding of this audio file (" + audioFormat.getEncoding() + ") is not supported.");

		BigInteger sampleInt = BigInteger.ZERO;
		BigInteger maxInt = BigInteger.ZERO;
		for (int b = 0; b < unsignedSampleBytes.length; b++) {
			sampleInt = sampleInt.shiftLeft(Byte.SIZE);
			sampleInt = sampleInt.add(BigInteger.valueOf(unsignedSampleBytes[b]));
			maxInt = maxInt.shiftLeft(Byte.SIZE);
			maxInt = maxInt.add(BigInteger.valueOf(0xFF));
		}

		return 2.0 * sampleInt.doubleValue() / maxInt.doubleValue() - 1.0;
	}

}
