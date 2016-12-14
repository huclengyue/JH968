package com.sunteam.recorder.recorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.media.AudioFormat;
import android.media.AudioRecord;

public class AudioRecorder {
	// �������ֽڴ�С
	private int bufferSizeInBytes = 0;

	// NewAudioName�ɲ��ŵ���Ƶ�ļ�
	private String NewAudioName = "";

	private AudioRecord audioRecord;
	private static boolean isRecord = false;// ��������¼�Ƶ�״̬

	private static AudioRecorder mInstance;

	private AudioRecorder() {

	}

	public synchronized static AudioRecorder getInstance() {
		if (mInstance == null)
			mInstance = new AudioRecorder();
		return mInstance;
	}

	public int startRecordAndFile() {
		// �ж��Ƿ����ⲿ�洢�豸sdcard
		if (AudioFileFunc.isSdcardExit()) {
			if (isRecord) {
				return ErrorCode.E_STATE_RECODING;
			} else {
				if (audioRecord == null) {
					creatAudioRecord();
				}
				audioRecord.startRecording();
				// ��¼��״̬Ϊtrue
				isRecord = true;
				// ������Ƶ�ļ�д���߳�
				new Thread(new AudioRecordThread()).start();
				return ErrorCode.SUCCESS;
			}
		} else {
			return ErrorCode.E_NOSDCARD;
		}
	}

	public void stopRecordAndFile() {
		close();
	}

	public long getRecordFileSize() {
		return AudioFileFunc.getFileSize(NewAudioName);
	}

	private void close() {
		if (audioRecord != null) {
			System.out.println("stopRecord");
			isRecord = false;// ֹͣ�ļ�д��
			audioRecord.stop();
			audioRecord.release();// �ͷ���Դ
			audioRecord = null;
		}
	}

	public boolean isRecord() {
		return isRecord;
	}

	private void creatAudioRecord() {
		// ��ȡ��Ƶ�ļ�·��
		NewAudioName = AudioFileFunc.getWavFilePath();

		// ��û������ֽڴ�С
		bufferSizeInBytes = AudioRecord.getMinBufferSize(
				AudioFileFunc.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
				AudioFormat.ENCODING_PCM_16BIT);

		// int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2
		// bytes we use only 1024
		// int BytesPerElement = 2; // 2 bytes in 16bit format

		// ����AudioRecord����
		audioRecord = new AudioRecord(AudioFileFunc.AUDIO_INPUT,
				AudioFileFunc.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
	}

	class AudioRecordThread implements Runnable {
		public void run() {
			writeDateTOFile();// ���ļ���д��������
			addHeader(NewAudioName);// �������ݼ���ͷ�ļ�
		}
	}

	/**
	 * ���ｫ����д���ļ������ǲ����ܲ��ţ���ΪAudioRecord��õ���Ƶ��ԭʼ������Ƶ��
	 */
	private void writeDateTOFile() {
		// newһ��byte����������һЩ�ֽ����ݣ���СΪ��������С
		byte[] audiodata = new byte[bufferSizeInBytes];
		FileOutputStream fos = null;
		int readsize = 0;
		try {
			File file = new File(NewAudioName);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);// ����һ���ɴ�ȡ�ֽڵ��ļ�
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (isRecord == true) {
			readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
			if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
				try {
					fos.write(audiodata);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			if (fos != null)
				fos.close();// �ر�д����
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �õ��ɲ��ŵ���Ƶ�ļ�
	 * @param inFilename
	 * @param outFilename
	 */
	private void addHeader(String outFilename) {
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = AudioFileFunc.AUDIO_SAMPLE_RATE;
		int channels = 2;
		long byteRate = 16 * AudioFileFunc.AUDIO_SAMPLE_RATE * channels / 8;
		try {
			RandomAccessFile raf = new RandomAccessFile(outFilename,"rw");
			totalAudioLen = raf.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			WriteWaveFileHeader(raf, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����ṩһ��ͷ��Ϣ��������Щ��Ϣ�Ϳ��Եõ����Բ��ŵ��ļ���
	 */
	private void WriteWaveFileHeader(RandomAccessFile out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}
}
