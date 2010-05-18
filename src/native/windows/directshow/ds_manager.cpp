/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

/**
 * \file ds_manager.cpp
 * \brief DirectShow capture devices manager.
 * \author Sebastien Vincent
 * \date 2010
 */

#include <cstdlib>

#include "ds_manager.h"
#include "ds_capture_device.h"
#include "qedit.h"

/* initialization of static member variables */
DSManager* DSManager::m_instance = NULL;

DSManager* DSManager::getInstance()
{
	return m_instance;
}

DSManager::DSManager()
{
	CoInitialize(NULL);
	initCaptureDevices();
}

DSManager::~DSManager()
{
	for(std::list<DSCaptureDevice*>::iterator it = m_devices.begin() ; it != m_devices.end() ; ++it)
	{
		delete *it;
	}
	m_devices.clear();

	/* one CoUninitialize per CoInitialize */
	CoUninitialize();
}

bool DSManager::initialize()
{
	if(!m_instance)
	{
		m_instance = new DSManager();
	}

	return m_instance != NULL;
}

void DSManager::destroy()
{
	if(m_instance)
	{
		delete m_instance;
        m_instance = NULL;
	}
}

std::list<DSCaptureDevice*> DSManager::getDevices() const
{
	return m_devices;
}

size_t DSManager::getDevicesCount()
{
	return m_devices.size();
}

void DSManager::initCaptureDevices()
{
	HRESULT ret = 0;
	VARIANT name;
	ICreateDevEnum* devEnum = NULL;
	IEnumMoniker* monikerEnum = NULL;
	IMoniker* moniker = NULL;

	/* clean up our list in case of reinitialization */
	for(std::list<DSCaptureDevice*>::iterator it = m_devices.begin() ; it != m_devices.end() ; ++it)
	{
		delete *it;
	}
	m_devices.clear();

	/* get the available devices list */
	ret = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER,
		IID_ICreateDevEnum, (void**)&devEnum);

	if(FAILED(ret))
	{
		return;
	}

	ret = devEnum->CreateClassEnumerator(CLSID_VideoInputDeviceCategory, 
		&monikerEnum, NULL);

	/* error or no devices */
	if(FAILED(ret) || ret == S_FALSE)
	{
		devEnum->Release();
		return;
	}

	/* loop and initialize all available capture devices */
	while(monikerEnum->Next(1, &moniker, 0) == S_OK)
	{
		DSCaptureDevice* captureDevice = NULL;
		IPropertyBag* propertyBag = NULL;

		/* get properties of the device */
		ret = moniker->BindToStorage(0, 0, IID_IPropertyBag, (void**)&propertyBag);
		if(!FAILED(ret))
		{
			VariantInit(&name);

			ret = propertyBag->Read(L"FriendlyName", &name, 0);
			if(FAILED(ret))
			{
				VariantClear(&name);
				propertyBag->Release();
				moniker->Release();
				continue;
			}

			/* create a new capture device */
			captureDevice = new DSCaptureDevice(name.bstrVal);
			/* wprintf(L"%ws\n", name.bstrVal); */

			if(captureDevice->initDevice(moniker))
			{
				/* initialization success, add to the list */
				m_devices.push_back(captureDevice);
			}
			else
			{
				printf("failed to initialize device\n");
				delete captureDevice;
			}

			/* clean up */
			VariantClear(&name);
			propertyBag->Release();
		}
		moniker->Release();
	}

	/* cleanup */
	devEnum->Release();
	monikerEnum->Release();
}
