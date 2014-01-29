//package com.welty.nboard;
//
///**
// * Created by IntelliJ IDEA.
// * User: HP_Administrator
// * Date: Jun 21, 2009
// * Time: 4:57:41 PM
// * To change this template use File | Settings | File Templates.
// */
//public class Pump {
//////////////////////////////////////////////
//// Pump class
//////////////////////////////////////////////
//
//
///** Class that pumps data from an input pipe to a window's message queue.
//*
//* This is intended to be run in a separate thread.
//*/
//class Pump {
//	Pump(HANDLE hReadFrom, HWND hwnd);
//	void Terminate();
//	void Run();
//
//	boolean m_fTerminated;
//	HANDLE m_hReadFrom;
//	HWND m_hwnd;
//
//	static int s_msgFromEngine;
//
//	HANDLE m_hWriteToEngine;
//	int m_ping;		//*< Last ping command sent to engine
//	int m_pong;		//*< Last pong command from engine
//	Pump m_pPump;	//*< the pump deletes itself after Terminate() is called.
//	String m_sName; //*< Name of the engine, for saving GGF files and eventually for display
//
///** This is the routine called when the engine's thread starts
//*
//* It runs the pump and deletes the pump when the pump is finished.
//* The input is a pointer to a boost::shared_ptr<Pump>, this is to keep
//* the pump around until both the thread and the main process are done with it.
//*/
//static DWORD WINAPI PumpThreadStart(void* data) {
//	boost::intrusive_ptr<Pump> pPump((Pump*)data);
//
//	pPump->Run();
//
//	return 0;
//}
//
///** Create a pump and start pumping.
//*/
//        Pump::Pump(HANDLE hReadFrom, HWND hwnd) : m_hReadFrom(hReadFrom), m_hwnd(hwnd), m_fTerminated(false) {
//	u4 threadId;
//	HANDLE hThread=CreateThread(NULL, 0, PumpThreadStart, this, 0, &threadId );
//
//	// this just decrements the refcount, it leaves the thread running.
//	if (hThread)
//		CloseHandle(hThread);
//}
//
///** This is called by the main thread to tell the pump to stop sending messages and shut down.
//*
//* Actual thread termination is done in the pump's thread, by polling the m_fTerminated variable.
//*/
//        void Pump::Terminate() {
//	m_fTerminated=true;
//}
//
///** Read the messages from the engine and post them to the main window's thread message queue.
//*
//* This function is running in a ubthread. m_pfTerminate is a pointer allocated by the main thread
//* but freed by the subthread. If set to 1, it tells the subthread to terminate.
//*
//* As the engine object may be deallocated before this subthread gets to access the
//*/
//        void Pump::Run() {
//	// read moves from ntest
//	final int buflen=1024;
//	char buffer[buflen];
//	char *pNextWrite=buffer, *pNextRead=buffer;
//	DWORD nRead;
//	while (ReadFile(m_hReadFrom, pNextWrite, DWORD(buffer+buflen-pNextWrite-1), &nRead, NULL) && !m_fTerminated) {
//		pNextWrite+=nRead;
//		pNextWrite[0]='\0';
//
//		// now check if we've got a complete line
//		while (char* pLineEnd=strchr(pNextRead, '\n')) {
//			// replace '\n' at end of line with '\0'. If line ends with "\r\n" replace with "\0\0".
//			pLineEnd[0]='\0';
//			if (pLineEnd>pNextRead && pLineEnd[-1]=='\r')
//				pLineEnd[-1]='\0';
//			String* ps=new String(pNextRead);
//			::PostMessage(m_hwnd, ReversiEngine::s_msgFromEngine, (WPARAM)ps, NULL);
//
//			pNextRead=pLineEnd+1;
//		}
//
//		// now get rid of the empty space in the buffer.
//		// can't use strcpy because source and dest overlap.
//		if (pNextRead>buffer) {
//			final size_t nReduce=pNextRead-buffer;
//			char* p;
//			for (p=buffer; pNextRead<=pNextWrite; pNextRead++, p++) {
//				p[0]=pNextRead[0];
//			}
//			for (; p<buffer+buflen; p++)
//				p[0]=0;
//			pNextWrite-=nReduce;
//			pNextRead=buffer;
//		}
//	}
//	if (!m_fTerminated) {
//		Z::MessageBox(IDS_ENGINE_TERMINATED, IDS_WARNING, MB_OK);
//		String *ps=new String("status Engine Terminated");
//		PostMessage(m_hwnd, ReversiEngine::s_msgFromEngine, (WPARAM)ps, NULL);
//	}
//
//	// save the hReadFrom pipe handle. We will close the pipe handle AFTER deleting this
//	// so m_hReadFrom will no longer be valid.
//	// we close the pipe afterwards because sometimes it takes a while.
//	CloseHandle(m_hReadFrom);
//
//}}
//};
//}
