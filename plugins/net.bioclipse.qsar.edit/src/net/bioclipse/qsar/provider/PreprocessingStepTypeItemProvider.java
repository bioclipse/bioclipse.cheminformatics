/**
 * Copyright (c) 2007-2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * www.eclipse.org•À½epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 * 
 *
 * $Id$
 */
package net.bioclipse.qsar.provider;
import java.util.Collection;
import java.util.List;
import net.bioclipse.qsar.PreprocessingStepType;
import net.bioclipse.qsar.QsarPackage;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
/**
 * This is the item provider adapter for a {@link net.bioclipse.qsar.PreprocessingStepType} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class PreprocessingStepTypeItemProvider
        extends ItemProviderAdapter
        implements
                IEditingDomainItemProvider,
                IStructuredItemContentProvider,
                ITreeItemContentProvider,
                IItemLabelProvider,
                IItemPropertySource {
        /**
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        public static final String copyright = "Copyright (c) 2007-2008 The Bioclipse Project and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nwww.eclipse.org\u00ef\u00bf\u03a9epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>\n\nContributors:\n    Ola Spjuth - initial API and implementation\n";
        /**
         * This constructs an instance from a factory and a notifier.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        public PreprocessingStepTypeItemProvider(AdapterFactory adapterFactory) {
                super(adapterFactory);
        }
        /**
         * This returns the property descriptors for the adapted class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
                if (itemPropertyDescriptors == null) {
                        super.getPropertyDescriptors(object);
                        addIdPropertyDescriptor(object);
                        addNamePropertyDescriptor(object);
                        addNamespacePropertyDescriptor(object);
                        addOrderPropertyDescriptor(object);
                        addVendorPropertyDescriptor(object);
                }
                return itemPropertyDescriptors;
        }
        /**
         * This adds a property descriptor for the Id feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addIdPropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_PreprocessingStepType_id_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_PreprocessingStepType_id_feature", "_UI_PreprocessingStepType_type"),
                                 QsarPackage.Literals.PREPROCESSING_STEP_TYPE__ID,
                                 true,
                                 false,
                                 false,
                                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                                 null,
                                 null));
        }
        /**
         * This adds a property descriptor for the Name feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addNamePropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_PreprocessingStepType_name_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_PreprocessingStepType_name_feature", "_UI_PreprocessingStepType_type"),
                                 QsarPackage.Literals.PREPROCESSING_STEP_TYPE__NAME,
                                 true,
                                 false,
                                 false,
                                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                                 null,
                                 null));
        }
        /**
         * This adds a property descriptor for the Namespace feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addNamespacePropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_PreprocessingStepType_namespace_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_PreprocessingStepType_namespace_feature", "_UI_PreprocessingStepType_type"),
                                 QsarPackage.Literals.PREPROCESSING_STEP_TYPE__NAMESPACE,
                                 true,
                                 false,
                                 false,
                                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                                 null,
                                 null));
        }
        /**
         * This adds a property descriptor for the Order feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addOrderPropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_PreprocessingStepType_order_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_PreprocessingStepType_order_feature", "_UI_PreprocessingStepType_type"),
                                 QsarPackage.Literals.PREPROCESSING_STEP_TYPE__ORDER,
                                 true,
                                 false,
                                 false,
                                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                                 null,
                                 null));
        }
        /**
         * This adds a property descriptor for the Vendor feature.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        protected void addVendorPropertyDescriptor(Object object) {
                itemPropertyDescriptors.add
                        (createItemPropertyDescriptor
                                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                                 getResourceLocator(),
                                 getString("_UI_PreprocessingStepType_vendor_feature"),
                                 getString("_UI_PropertyDescriptor_description", "_UI_PreprocessingStepType_vendor_feature", "_UI_PreprocessingStepType_type"),
                                 QsarPackage.Literals.PREPROCESSING_STEP_TYPE__VENDOR,
                                 true,
                                 false,
                                 false,
                                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                                 null,
                                 null));
        }
        /**
         * This returns PreprocessingStepType.gif.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public Object getImage(Object object) {
                return overlayImage(object, getResourceLocator().getImage("full/obj16/PreprocessingStepType"));
        }
        /**
         * This returns the label text for the adapted class.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public String getText(Object object) {
                String label = ((PreprocessingStepType)object).getName();
                return label == null || label.length() == 0 ?
                        getString("_UI_PreprocessingStepType_type") :
                        getString("_UI_PreprocessingStepType_type") + " " + label;
        }
        /**
         * This handles model notifications by calling {@link #updateChildren} to update any cached
         * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public void notifyChanged(Notification notification) {
                updateChildren(notification);
                switch (notification.getFeatureID(PreprocessingStepType.class)) {
                        case QsarPackage.PREPROCESSING_STEP_TYPE__ID:
                        case QsarPackage.PREPROCESSING_STEP_TYPE__NAME:
                        case QsarPackage.PREPROCESSING_STEP_TYPE__NAMESPACE:
                        case QsarPackage.PREPROCESSING_STEP_TYPE__ORDER:
                        case QsarPackage.PREPROCESSING_STEP_TYPE__VENDOR:
                                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
                                return;
                }
                super.notifyChanged(notification);
        }
        /**
         * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
         * that can be created under this object.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
                super.collectNewChildDescriptors(newChildDescriptors, object);
        }
        /**
         * Return the resource locator for this item provider's resources.
         * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
         * @generated
         */
        @Override
        public ResourceLocator getResourceLocator() {
                return QsarEditPlugin.INSTANCE;
        }
}
